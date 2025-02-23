package team.klover.server.domain.member.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import team.klover.server.domain.member.v1.enums.SocialProvider;

@AllArgsConstructor
@Getter
@Builder
public class MobileSocialLoginParam {
    private String email;
    private String providerId;
    private String nickname;
    private SocialProvider provider;
}
