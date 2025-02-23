package team.klover.server.domain.member.v1.dto;

import lombok.*;
import team.klover.server.domain.member.v1.entity.Member;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {
    private String email;
    private String nickname;
    private String profileUrl;
    private String role;
    private Long id;

    public MemberDto(Member member) {
        id = member.getId();
        nickname = member.getNickname();
        profileUrl = member.getProfileUrl();
        role = member.getRole().name();
        email = member.getEmail();
    }
}