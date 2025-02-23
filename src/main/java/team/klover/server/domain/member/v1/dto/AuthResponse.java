package team.klover.server.domain.member.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class AuthResponse {
    private String accessToken;;
    private String refreshToken;
}
