package team.klover.server.domain.chat.chatMessage.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.klover.server.domain.chat.chatMessage.entity.ChatMessage;
import team.klover.server.domain.chat.chatMessage.entity.MessageContent;
import team.klover.server.domain.chat.chatRoom.entity.ChatRoom;
import team.klover.server.domain.member.v1.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 해당 채팅방의 메시지 페이지 조회
    Page<ChatMessage> findByChatRoomId(Long chatRoomId, Pageable pageable);
    //Page<ChatMessage> findByChatRoomIdAndCreateDateLessThan(Long chatRoomId, LocalDateTime pointTime, Pageable pageable);

    // 해당 채팅방의 메시지 리스트 조회
    List<ChatMessage> findByChatRoom(ChatRoom chatRoom);

    // 가장 최근 메시지 조회
    ChatMessage findTopByChatRoomIdOrderByIdDesc(Long chatRoomId);

    // 해당 채팅방의 해당 메시지 이후에 작성된 메시지 리스트 조회
    List<ChatMessage> findByChatRoomIdAndIdGreaterThan(Long chatRoomId, Long lastReadMessageId);

    List<ChatMessage> findAllByMember(Member member);

    // 가장 최근 메시지의 ID를 가져오는 메서드
    @Query("SELECT cm.id FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId ORDER BY cm.createDate DESC LIMIT 1")
    Optional<Long> findLatestMessageIdByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
