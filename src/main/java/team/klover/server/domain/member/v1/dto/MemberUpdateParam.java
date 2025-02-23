package team.klover.server.domain.member.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class MemberUpdateParam {
    private Long memberId;
    private String nickname; //new nickname
    private String profileUrl; //oldProfileUrl
    private String country;

    public void changeProfileUrl(String newUrl) {
        this.profileUrl = newUrl;
    }

}
