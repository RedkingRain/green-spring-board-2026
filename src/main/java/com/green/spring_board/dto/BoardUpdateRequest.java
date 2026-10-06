package com.green.spring_board.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// lombok
@AllArgsConstructor // 모든 변수를 사용하는 생성자를 자동완성 시켜주는 어노테이션
@NoArgsConstructor  // 어떠한 변수도 사용하지 않는 기본 생성자를 자동완성 시켜주는 어노테이션
@Getter             // 모든 변수들에 대해 Getter를 자동완성 시켜주는 어노테이션, 변수 이름 위에 적용시키면 해당 변수들만 적용 가능
@Setter             // 모든 변수들에 대해 Setter를 자동완성 시켜주는 어노테이션, 변수 이름 위에 적용시키면 해당 변수들만 적용 가능
public class BoardUpdateRequest {
    // Board 수정의 경우
    // 수정하려는 필드 값만 요청에 담아보낸다.
    // NotBlank를 붙이면 수정(Patch) API 용도와 다르게 모든 필드를 다 채워줘야 하는 문제가 발생한다.
//    @NotBlank
    @Size(min = 10, max = 50)   // null 값을 제외한, 공백 및 기타 입력이 있을 경우 값의 길이 검사 실행하는 규칙
    private String title;
//    @NotBlank
    @Size(min = 10)
    private String content;
}
