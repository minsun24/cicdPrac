package com.lgcns.pipeline.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistDTO {
    @Email
    @NotBlank
    private String email;

    @Size(min = 8, max = 16, message = "암호는 8자리 이상 20자리 이하로 입력하세요!")
    private String password;

    @NotBlank
    private String name;    // 초기 가입은 기본적으로 USER 권한으로. 관리자(ADMIN)은 추후 UPDATE 해주는 방식
}
