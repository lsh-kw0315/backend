package team.klover.server.domain.member.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SignupRequestDto {
    private String email;
    private String password;
    private String nickname;
}
