package com.green.spring_board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SignupRequest {
    @NotBlank
    @Email  // 값의 형식이 email 형식인지 검증
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(max = 6)
    private String password;

    @NotBlank
    @Size(max = 30)
    private String nickname;
}
