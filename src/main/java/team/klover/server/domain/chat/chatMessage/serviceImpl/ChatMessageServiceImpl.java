package team.klover.server.domain.chat.chatMessage.serviceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.klover.server.domain.chat.chatMessage.dto.req.ChatMessageForm;
import team.klover.server.domain.chat.chatMessage.dto.res.ChatMessageDto;
import team.klover.server.domain.chat.chatMessage.entity.ChatMessage;
import team.klover.server.domain.chat.chatMessage.entity.ChatMessagePage;
import team.klover.server.domain.chat.chatMessage.entity.MessageContent;
import team.klover.server.domain.chat.chatMessage.repository.ChatMessageRepository;
import team.klover.server.domain.chat.chatMessage.repository.MessageContentRepository;
import team.klover.server.domain.chat.chatMessage.service.ChatMessageService;
import team.klover.server.domain.chat.chatRoom.entity.ChatRoom;
import team.klover.server.domain.chat.chatRoom.entity.ChatRoomMember;
import team.klover.server.domain.chat.chatRoom.repository.ChatRoomMemberRepository;
import team.klover.server.domain.chat.chatRoom.repository.ChatRoomRepository;
import team.klover.server.domain.member.v1.entity.Member;
import team.klover.server.domain.member.v1.repository.MemberV1Repository;
import team.klover.server.global.exception.KloverRequestException;
import team.klover.server.global.exception.ReturnCode;
import team.klover.server.global.s3.S3Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberV1Repository memberV1Repository;
    private final RabbitTemplate rabbitTemplate;
    private final MessageContentRepository messageContentRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final S3Service s3Service;

    // 해당 채팅방의 메시지 실시간 조회 시작
    @Override
    @Transactional
    public Page<ChatMessageDto> findByChatRoomId(Long currentMemberId, Long chatRoomId, Pageable pageable) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        checkPageSize(pageable.getPageSize());

        // 채팅방 참여멤버만 메시지 조회 가능
        boolean memberExists = chatRoom.getChatRoomMembers().stream()
                .anyMatch(joinedMember -> joinedMember.getMember().getId().equals(currentMemberId));
        if (!memberExists) {
            throw new KloverRequestException(ReturnCode.NOT_AUTHORIZED);
        }
        // 해당 채팅방 내의 모든 메시지 읽음 처리 & 해당 채팅방 메시지 가져오기
        readAllChatMessages(currentMemberId, chatRoom, chatRoomId);
        Page<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomId(chatRoomId, pageable);

        // MongoDB에서 메시지 내용 불러오기
        List<String> stringMessageIds = chatMessages.stream()
                .map(chatMessage -> String.valueOf(chatMessage.getId()))
                .collect(Collectors.toList());
        Map<Long, String> messageContentMap = messageContentRepository.findByIdIn(stringMessageIds)
                .stream()
                .filter(Objects::nonNull) // null 값 필터링
                .collect(Collectors.toMap(
                        message -> Long.parseLong(message.getId()),
                        message -> Objects.requireNonNullElse(message.getContent(), "") // null이면 빈 문자열 처리
                ));

        return chatMessages.map(chatMessage -> {
            String content = messageContentMap.getOrDefault(chatMessage.getId(), ""); // 없으면 빈 문자열
            return convertToChatMessageDto(chatMessage, content);
        });
    }

    // 해당 채팅방에서 메시지 검색(닉네임/내용)
    @Override
    @Transactional
    public Page<ChatMessageDto> searchByKeyword(Long currentMemberId, Long chatRoomId, String keyword, Pageable pageable){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        checkPageSize(pageable.getPageSize());

        // 채팅방 참여멤버만 메시지 조회 가능
        boolean memberExists = chatRoom.getChatRoomMembers().stream()
                .anyMatch(joinedMember -> joinedMember.getMember().getId().equals(currentMemberId));
        if (!memberExists) {
            throw new KloverRequestException(ReturnCode.NOT_AUTHORIZED);
        }
        // 1️⃣ 채팅방 내 모든 메시지 ID 조회
        Page<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomId(chatRoomId, pageable);
        List<String> stringMessageIds = chatMessages.stream()
                .map(chatMessage -> String.valueOf(chatMessage.getId()))
                .collect(Collectors.toList());

        // 2️⃣ MongoDB에서 해당 ID들의 메시지 내용 조회
        Map<Long, String> messageContentMap = messageContentRepository.findByIdIn(stringMessageIds).stream()
                .collect(Collectors.toMap(message -> Long.parseLong(message.getId()), MessageContent::getContent));

        // 3️⃣ 키워드 포함 여부 검사 후 필터링
        List<ChatMessage> filteredMessages = chatMessages.stream()
                .filter(chatMessage -> {
                    String content = messageContentMap.getOrDefault(chatMessage.getId(), "");
                    return content.contains(keyword); // 키워드 포함 여부 확인
                })
                .collect(Collectors.toList());

        // 4️⃣ 필터링된 메시지를 DTO로 변환
        List<ChatMessageDto> chatMessageDtos = filteredMessages.stream()
                .map(chatMessage -> {
                    String content = messageContentMap.getOrDefault(chatMessage.getId(), "");
                    return convertToChatMessageDto(chatMessage, content);
                })
                .collect(Collectors.toList());

        // 5️⃣ Page 객체로 변환 후 반환
        return new PageImpl<>(chatMessageDtos, pageable, chatMessageDtos.size());
    }

    // 해당 채팅방에서 메시지 생성
    @Override
    @Transactional
    public void writeChatMessage(Long currentMemberId, Long chatRoomId, ChatMessageForm chatMessageForm, List<MultipartFile> imageFiles) {
        // 현재 로그인한 사용자의 member 객체를 가져오는 메서드
        Member member = memberV1Repository.findById(currentMemberId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        // 채팅방 참여멤버만 메시지 생성 가능
        boolean memberExists = chatRoom.getChatRoomMembers().stream()
                .anyMatch(joinedMember -> joinedMember.getMember().getId().equals(currentMemberId));
        if (!memberExists) {
            throw new KloverRequestException(ReturnCode.NOT_AUTHORIZED);
        }

        // 입력 받은 이미지들 S3에 저장
        if (imageFiles == null) { imageFiles = new ArrayList<>(); }  // imageFiles가 null이면 빈 리스트로 초기화
        List<String> imageUrls = new ArrayList<>();
        if (imageFiles.size() > 4) {
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        } else {
            if (!imageFiles.isEmpty()) {
                for (MultipartFile imageFile : imageFiles) {
                    if (!imageFile.isEmpty()) {  // 파일이 비어 있는지 확인
                        try {
                            String imageUrl = s3Service.uploadFile(imageFile, "commPost-images");
                            imageUrls.add(imageUrl);
                        } catch (IOException e) {
                            throw new KloverRequestException(ReturnCode.INTERNAL_ERROR);
                        }
                    } else {
                        log.warn("Empty file received, skipping upload.");
                    }
                }
            }
        }
        Long inactiveMemberNum = countInactiveMembers(chatRoomId);
        ChatMessage chatMessage = ChatMessage.builder()
                .member(member)
                .chatRoom(chatRoom)
                .imageUrls(imageUrls)
                .readCount(inactiveMemberNum)
                .build();
        chatMessageRepository.save(chatMessage);

        // MongoDB에 메시지 본문 저장
        String content = (chatMessageForm != null) ? chatMessageForm.getContent() : "";
        MessageContent messageContent = MessageContent.builder()
                .id(String.valueOf(chatMessage.getId())) // ChatMessage의 ID를 키로 사용
                .content(content)
                .build();
        messageContentRepository.save(messageContent);

        rabbitTemplate.convertAndSend("amq.topic", "chatRoomId: " + chatRoomId + ",MessageCreated: ",
                convertToChatMessageDto(chatMessage, messageContent.getContent()));
    }

    // 해당 메시지 삭제
    @Override
    @Transactional
    public void deleteChatMessage(Long currentMemberId, Long messageId) {
        ChatMessage chatMessage = chatMessageRepository.findById(messageId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        // 작성자만 삭제 가능
        if (!chatMessage.getMember().getId().equals(currentMemberId)) {
            throw new KloverRequestException(ReturnCode.NOT_AUTHORIZED);
        }
        s3Service.deleteAllFile(chatMessage.getImageUrls());
        messageContentRepository.deleteById(String.valueOf(messageId));

        chatMessageRepository.delete(chatMessage);
    }

    // 해당 채팅방의 모든 메시지 삭제
    @Override
    @Transactional
    public void deleteAllChatMessages(Long chatRoomId){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));
        List<ChatMessage> messages = chatMessageRepository.findByChatRoom(chatRoom);
        messages.forEach(message -> s3Service.deleteAllFile(message.getImageUrls()));
        messages.forEach(message -> messageContentRepository.deleteById(String.valueOf(message.getId())));

        chatMessageRepository.deleteAll(messages);
    }

    // 해당 채팅방의 메시지 실시간 조회 중단
    @Override
    @Transactional
    public void updateLastReadMessage(Long currentMemberId, Long chatRoomId) {
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByMemberIdAndChatRoomId(currentMemberId, chatRoomId);
        chatRoomMember.setActive(false);

        ChatMessage lastReadMessage = chatMessageRepository.findTopByChatRoomIdOrderByIdDesc(chatRoomId);
        chatRoomMember.setLastReadMessageId(lastReadMessage != null ? lastReadMessage.getId() : null);
        chatRoomMemberRepository.save(chatRoomMember);
    }

    // 해당 채팅방 내의 모든 메시지 읽음 처리
    @Transactional
    public void readAllChatMessages(Long currentMemberId, ChatRoom chatRoom, Long chatRoomId){
        ChatRoomMember chatRoomMember = chatRoom.getChatRoomMembers().stream()
                .filter(member -> member.getMember().getId().equals(currentMemberId))
                .findFirst()
                .orElseThrow(() -> new KloverRequestException(ReturnCode.NOT_FOUND_ENTITY));

        // 채팅방 미참여중인 멤버가 참여 했을때만 실행
        if (!chatRoomMember.isActive()) {
            Long lastReadMessageId = chatRoomMember.getLastReadMessageId();
            List<ChatMessage> chatMessages;
            if (lastReadMessageId == null) {
                // 처음 들어오는 경우, 모든 메시지의 readCount -1 처리
                chatMessages = chatMessageRepository.findByChatRoom(chatRoom);
            } else {
                // 마지막으로 읽은 메시지 이후에 생성된 메시지들만 readCount -1 처리
                chatMessages = chatMessageRepository.findByChatRoomIdAndIdGreaterThan(chatRoomId, lastReadMessageId);
            }
            List<ChatMessage> updatedMessages = chatMessages.stream()
                    .peek(chatMessage -> {
                        if (chatMessage.getReadCount() > 0) {
                            chatMessage.setReadCount(chatMessage.getReadCount() - 1);
                        }
                    })
                    .collect(Collectors.toList());
            chatMessageRepository.saveAll(updatedMessages);
        }
        chatRoomMember.setActive(true);

        // 마지막으로 읽은 메시지ID 초기화
        ChatMessage lastReadMessage = chatMessageRepository.findTopByChatRoomIdOrderByIdDesc(chatRoomId);
        chatRoomMember.setLastReadMessageId(lastReadMessage != null ? lastReadMessage.getId() : null);
        chatRoomMemberRepository.save(chatRoomMember);
    }

    // 해당 채팅방에 실시간 참여중이 아닌 인원수(lastReadMessageId=null인 chatRoomMember 인원수)
    @Transactional(readOnly = true)
    public Long countInactiveMembers(Long chatRoomId){
        return chatRoomMemberRepository.countInActiveMembers(chatRoomId);
    }

    // 요청 메시지 수 제한
    private void checkPageSize(int pageSize) {
        int maxPageSize = ChatMessagePage.getMaxPageSize();
        if (pageSize > maxPageSize) {
            throw new KloverRequestException(ReturnCode.WRONG_PARAMETER);
        }
    }

    // ChatMessage를 ChatMessageDto로 변환
    private ChatMessageDto convertToChatMessageDto(ChatMessage chatMessage, String content) {
        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .memberId(chatMessage.getMember().getId())
                .nickname(chatMessage.getMember().getNickname())
                .profileImageUrl(chatMessage.getMember().getProfileUrl())
                .content(content)
                .imageUrls(chatMessage.getImageUrls())
                .readCount(chatMessage.getReadCount())
                .createDate(chatMessage.getCreateDate())
                .build();
    }
}
