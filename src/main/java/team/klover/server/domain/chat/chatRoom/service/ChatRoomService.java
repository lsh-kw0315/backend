package team.klover.server.domain.chat.chatRoom.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import team.klover.server.domain.chat.chatRoom.dto.req.ChatRoomBatchActionForm;
import team.klover.server.domain.chat.chatRoom.dto.req.ChatRoomCreateForm;
import team.klover.server.domain.chat.chatRoom.dto.req.ChatRoomForm;
import team.klover.server.domain.chat.chatRoom.dto.req.ChatRoomTitleUpdateForm;
import team.klover.server.domain.chat.chatRoom.dto.res.ChatRoomDto;

public interface ChatRoomService {
    // 채팅방 목록 조회(DM/그룹)
    Page<ChatRoomDto> findByMemberId(Long currentMemberId, Pageable pageable);

    // 채팅방 생성(DM/그룹)
    ChatRoomDto addChatRoom(Long currentMemberId, @Valid ChatRoomForm chatRoomForm);

    //void addChatRoom(Long currentMemberId, @Valid ChatRoomCreateForm chatRoomCreateForm);

    // 채팅방 이름 수정(그룹)
    void updateChatRoomTitle(Long chatRoomId, Long currentMemberId, @Valid ChatRoomForm chatRoomForm);

    //void updateChatRoomTitle(Long chatRoomId, Long currentMemberId, @Valid ChatRoomTitleUpdateForm chatRoomTitleUpdateForm);

    // 채팅방에 초대(그룹)
    void inviteChatRoomMember(Long chatRoomId, Long currentMemberId, @Valid ChatRoomForm chatRoomForm);

    //void inviteChatRoomMember(Long chatRoomId, Long currentMemberId, @Valid ChatRoomBatchActionForm chatRoomBatchActionForm);

    // 채팅방에서 강퇴(그룹) / 방장권한
    void kickOutChatRoomMember(Long chatRoomId, Long currentMemberId, @Valid ChatRoomForm chatRoomForm);

    //void kickOutChatRoomMember(Long chatRoomId, Long currentMemberId, @Valid ChatRoomBatchActionForm chatRoomBatchActionForm);

    // 채팅방 나가기(DM/그룹)
    void leaveChatRoomMember(Long chatRoomId, Long currentMemberId);
}
