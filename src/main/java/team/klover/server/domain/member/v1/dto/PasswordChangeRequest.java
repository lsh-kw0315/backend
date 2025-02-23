package com.ll.server.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;
}
