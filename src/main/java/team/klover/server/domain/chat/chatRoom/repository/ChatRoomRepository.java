package team.klover.server.domain.chat.chatRoom.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.klover.server.domain.chat.chatRoom.entity.ChatRoom;
import team.klover.server.domain.member.v1.entity.Member;
import team.klover.server.domain.chat.chatRoom.entity.ChatRoomMember;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 사용자가 참여 중인 채팅방 목록 조회
    Page<ChatRoom> findByChatRoomMembers_Member(Member member, Pageable pageable);

    // 현재 사용자가 otherMember와 이미 1대1 채팅방이 존재하는지 확인
    @Query("SELECT EXISTS (" +
            "    SELECT 1 FROM ChatRoom c " +
            "    JOIN ChatRoomMember m1 ON c.id = m1.chatRoom.id " +
            "    JOIN ChatRoomMember m2 ON c.id = m2.chatRoom.id " +
            "    WHERE m1.member.id = :memberId1 " +
            "    AND m2.member.id = :memberId2 " +
            "    AND c.id IN (" +
            "        SELECT cm.chatRoom.id FROM ChatRoomMember cm " +
            "        GROUP BY cm.chatRoom.id " +
            "        HAVING COUNT(cm.chatRoom.id) = 2" +
            "    )" +
            ")")
    boolean existsOneOnOneChatRoom(@Param("memberId1") Long memberId1, @Param("memberId2") Long memberId2);

    // 사용자가 참여 중인 채팅방 목록을 최신 메시지 순으로 조회
    @Query("""
    SELECT cr
    FROM ChatRoom cr
    JOIN cr.chatRoomMembers crm
    LEFT JOIN ChatMessage cm ON cm.chatRoom = cr
    WHERE crm.member = :member
    GROUP BY cr, crm
    ORDER BY COALESCE(MAX(cm.createDate), cr.createDate) DESC
""")
    Page<ChatRoom> findChatRoomsByMemberOrderByLatestMessage(@Param("member") Member member, Pageable pageable);

    List<ChatRoom> findAllByMember(Member member);

    @Query("SELECT crm FROM ChatRoomMember crm JOIN FETCH crm.member WHERE crm.chatRoom.id = :chatRoomId")
    List<ChatRoomMember> findChatRoomMembersWithMember(@Param("chatRoomId") Long chatRoomId);
}
