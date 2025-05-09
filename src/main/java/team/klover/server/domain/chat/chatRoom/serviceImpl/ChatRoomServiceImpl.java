package team.klover.server.domain.chat.chatRoom.serviceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.klover.server.domain.chat.chatMessage.entity.MessageContent;
import team.klover.server.domain.chat.chatMessage.repository.ChatMessageRepository;
import team.klover.server.domain.chat.chatMessage.repository.MessageContentRepository;
import team.klover.server.domain.chat.chatMessage.service.ChatMessageService;
import team.klover.server.domain.chat.chatRoom.dto.req.ChatRoomBatchActionForm;
import team.klover.server.domain.chat.chatRoom.dto.req.ChatRoomCreateForm;
import team.klover.server.domain.chat.chatRoom.dto.req.ChatRoomForm;
import team.klover.server.domain.chat.chatRoom.dto.req.ChatRoomTitleUpdateForm;
import team.klover.server.domain.chat.chatRoom.dto.res.ChatRoomDto;
import team.klover.server.domain.chat.chatRoom.dto.res.MemberInfoDto;
import team.klover.server.domain.chat.chatRoom.entity.ChatRoom;
import team.klover.server.domain.chat.chatRoom.entity.ChatRoomMember;
import team.klover.server.domain.chat.chatRoom.entity.ChatRoomPage;
import team.klover.server.domain.chat.chatRoom.repository.ChatRoomRepository;
import team.klover.server.domain.chat.chatRoom.service.ChatRoomService;
import team.klover.server.domain.member.v1.entity.Member;
import team.klover.server.domain.member.v1.repository.MemberV1Repository;
import team.klover.server.global.exception.KloverRequestException;
import team.klover.server.global.exception.ReturnCode;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final MemberV1Repository memberV1Repository;
    private final ChatMessageRepository chatMessageRepository;
    private final MessageContentRepository messageContentRepository;
    private final ChatMessageService chatMessageService;

    // 채팅방 목록 조회(DM/그룹)
    @Override
    @Transactional(readOnly = true)
    public Page<ChatRoomDto> findByMemberId(Long currentMemberId, Pageable pageable){
        // 현재 로그인한 사용자의 member 객체를 가져오는 메서드
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        checkPageSize(pageable.getPageSize());

        // 사용자가 참여 중인 채팅방 목록을 최신 메시지 순으로 조회
        Page<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByMemberOrderByLatestMessage(member, pageable);
        return chatRooms.map(this::convertToChatRoomDto);
    }

    // 채팅방 생성(DM/그룹)
    @Override
    @Transactional
    public ChatRoomDto addChatRoom(Long currentMemberId, @Valid ChatRoomForm chatRoomForm){
        // 현재 로그인한 사용자의 member 객체를 가져오는 메서드
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        // 제한 인원 초과 여부 확인 (본인 제외)
        if (chatRoomForm.getChatRoomMembers().size() > ChatRoom.ROOM_MEMBER_LIMIT - 1) {
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }

        // 1대1 채팅방 중복 방지 로직
        if (chatRoomForm.getChatRoomMembers().size() == 1) { // 1대1 채팅인지 확인
            Long otherMemberId = chatRoomForm.getChatRoomMembers().get(0).getMember().getId();

            // 현재 사용자가 otherMember와 이미 1대1 채팅방이 존재하는지 확인
            boolean exists = chatRoomRepository.existsOneOnOneChatRoom(currentMemberId, otherMemberId);
            if (exists) {
                throw new KloverRequestException(ReturnCode.ALREADY_EXIST);
            }
        }

        // 새로운 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .title(chatRoomForm.getTitle())
                .member(member) // 방장 지정
                .build();
        // 본인을 맨 앞에 추가
        List<ChatRoomMember> chatRoomMembers = new ArrayList<>();

        ChatRoomMember enterChatRoomMyself = ChatRoomMember.builder()
                .member(member)
                .chatRoom(chatRoom)
                .build();
        chatRoomMembers.add(enterChatRoomMyself);

        // 추가하려는 멤버들 중 중복되지 않는 멤버만 추가
        Set<Long> addedMemberIds = new HashSet<>();
        addedMemberIds.add(member.getId()); // 본인 ID 추가
        for (ChatRoomMember chatRoomMember : chatRoomForm.getChatRoomMembers()) {
            Long memberId = chatRoomMember.getMember().getId();
            if (!addedMemberIds.contains(memberId)) { // 중복 방지
                ChatRoomMember memberInChatRoom = ChatRoomMember.builder()
                        .member(chatRoomMember.getMember())
                        .chatRoom(chatRoom)
                        .build();
                chatRoomMembers.add(memberInChatRoom);
                //중복 체크용
                addedMemberIds.add(memberId);
            } else {
                throw new KloverRequestException(ReturnCode.ALREADY_EXIST);
            }
        }
        chatRoom.setChatRoomMembers(chatRoomMembers);
        chatRoomRepository.save(chatRoom);
        return convertToChatRoomDto(chatRoom);
    }

    // 채팅방 이름 수정(그룹)
    @Override
    @Transactional
    public void updateChatRoomTitle(Long chatRoomId, Long currentMemberId, @Valid ChatRoomForm chatRoomForm){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        // 채팅방 멤버 누구나 수정 가능함
        boolean memberExists = chatRoom.getChatRoomMembers().stream()
                .anyMatch(joinedMember -> joinedMember.getMember().getId().equals(currentMemberId));
        if (!memberExists) {
            throw new KloverRequestException(ReturnCode.NOT_AUTHORIZED);
        }
        chatRoom.setTitle(chatRoomForm.getTitle());
        chatRoomRepository.save(chatRoom);
    }

    // 채팅방에 초대(그룹)
    @Override
    @Transactional
    public void inviteChatRoomMember(Long chatRoomId, Long currentMemberId, @Valid ChatRoomForm chatRoomForm){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        // 채팅방 멤버 누구나 초대 가능함
        boolean memberExists = chatRoom.getChatRoomMembers().stream()
                .anyMatch(joinedMember -> joinedMember.getMember().getId().equals(currentMemberId));
        if (!memberExists) {
            throw new KloverRequestException(ReturnCode.NOT_AUTHORIZED);
        }
        // 제한 인원 초과 여부 확인 (본인 제외)
        if (chatRoomForm.getChatRoomMembers().size() > ChatRoom.ROOM_MEMBER_LIMIT - 1) {
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }

        // 초대할 멤버 추가(기존 멤버 아닌 경우에만 추가)
        List<ChatRoomMember> newMembers = chatRoomForm.getChatRoomMembers().stream()
                .filter(newMember -> chatRoom.getChatRoomMembers().stream()
                        .noneMatch(existingMember -> existingMember.getMember().getId().equals(newMember.getMember().getId())))
                .peek(newMember -> newMember.setChatRoom(chatRoom)) // chatRoom 설정
                .collect(Collectors.toList());
        chatRoom.getChatRoomMembers().addAll(newMembers);

    }

    // 채팅방에서 강퇴(그룹) / 방장권한
    @Override
    @Transactional
    public void kickOutChatRoomMember(Long chatRoomId, Long currentMemberId, @Valid ChatRoomForm chatRoomForm){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        // 방장인지 확인 (방장만 강퇴 가능)
        if (!chatRoom.getMember().getId().equals(currentMemberId)) {
            throw new KloverRequestException(ReturnCode.NOT_AUTHORIZED);
        }

        // 강퇴할 멤버 목록 가져오기 & 채팅방 참여 목록에서 제거
        List<Long> kickMemberIds = chatRoomForm.getChatRoomMembers().stream()
                .map(member -> member.getMember().getId())
                .collect(Collectors.toList());
        // 방장은 자기자신을 강퇴하지 못함
        if (kickMemberIds.contains(currentMemberId)) {
            throw new KloverRequestException(ReturnCode.INVALID_REQUEST);
        }

        chatRoom.getChatRoomMembers().removeIf(existingMember ->
                kickMemberIds.contains(existingMember.getMember().getId()));

        // 강퇴 후 남은 인원이 2명 미만이면 채팅방 삭제
        if (chatRoom.getChatRoomMembers().size() < 2) {
            chatRoomRepository.delete(chatRoom);

        } else {
            chatRoomRepository.save(chatRoom);
        }
    }

    // 채팅방 나가기(DM/그룹)
    @Override
    @Transactional
    public void leaveChatRoomMember(Long chatRoomId, Long currentMemberId){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        // 현재 멤버가 속한 ChatRoomMember 찾기
        ChatRoomMember chatRoomMember = chatRoom.getChatRoomMembers().stream()
                .filter(member -> member.getMember().getId().equals(currentMemberId))
                .findFirst()
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        chatRoom.getChatRoomMembers().remove(chatRoomMember);  // ChatRoomMember 제거


        // 방장인지 확인
        if (chatRoom.getMember().getId().equals(currentMemberId)) {
            if (chatRoom.getChatRoomMembers().size() > 1) { // 2명 이상 남아있으면 다음 방장 지정
                ChatRoomMember nextOwner = chatRoom.getChatRoomMembers().get(0);
                chatRoom.setMember(nextOwner.getMember());
            } else { // 1명 미만(0명)이면 채팅방 삭제
                chatMessageService.deleteAllChatMessages(chatRoomId);
                chatRoomRepository.delete(chatRoom);
                return;
            }
        } else if (chatRoom.getChatRoomMembers().size() < 2) { // 방장이 아닌데 나갔을 때 1명이 되면 삭제
            chatMessageService.deleteAllChatMessages(chatRoomId);
            chatRoomRepository.delete(chatRoom);
            return;
        }
        chatRoomRepository.save(chatRoom);
    }

    // 요청 페이지 수 제한
    private void checkPageSize(int pageSize) {
        int maxPageSize = ChatRoomPage.getMaxPageSize();
        if (pageSize > maxPageSize) {
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }
    }

    // ChatRoom을 ChatRoomDto로 변환
    private ChatRoomDto convertToChatRoomDto(ChatRoom chatRoom) {
        // Fetch Join으로 가져온 ChatRoomMembers 사용
        List<ChatRoomMember> chatRoomMembers = chatRoomRepository.findChatRoomMembersWithMember(chatRoom.getId());

        // 가장 최근 메시지의 ID 조회
        Optional<Long> latestMessageId = chatMessageRepository.findLatestMessageIdByChatRoomId(chatRoom.getId());

        // 해당 메시지 ID로 MongoDB에서 메시지 내용 조회
        String lastMessage = latestMessageId
                .flatMap(id -> messageContentRepository.findById(id.toString())) // MongoDB에서 메시지 조회
                .map(MessageContent::getContent) // content 값 가져오기
                .orElse(""); // 메시지가 없으면 빈 문자열 반환

        return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .memberId(chatRoom.getMember().getId())
                .title(chatRoom.getTitle())
                .memberCount(chatRoomMembers.size()) // 여기서 변경
                .chatRoomMembers(chatRoomMembers.stream() // 여기서도 변경
                        .map(chatRoomMember -> MemberInfoDto.builder()
                                .memberId(chatRoomMember.getMember().getId())
                                .memberNickname(chatRoomMember.getMember().getNickname()) // 이제 정상적으로 가져올 수 있음
                                .build()
                        )
                        .collect(Collectors.toList()))
                .lastMessage(lastMessage)
                .build();
    }
}
