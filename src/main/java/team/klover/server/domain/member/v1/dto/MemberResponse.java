package com.ll.server.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberResponse {
    private Long id;
    private String email;
    private String nickname;
    private String profileUrl;
    private String role;
}
