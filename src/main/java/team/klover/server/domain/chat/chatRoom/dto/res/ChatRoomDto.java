package team.klover.server.domain.chat.chatRoom.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDto {
    private Long id;
    private Long memberId;
    private String title;
    private List<MemberInfoDto> chatRoomMembers;
    private String lastMessage;
    private int memberCount;
}
