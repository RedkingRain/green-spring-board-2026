package com.green.spring_board;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "boards") // 실제 테이블 명 기입
// lombok
@AllArgsConstructor // 모든 변수를 사용하는 생성자를 자동완성 시켜주는 어노테이션
@NoArgsConstructor  // 어떠한 변수도 사용하지 않는 기본 생성자를 자동완성 시켜주는 어노테이션
@Getter             // 모든 변수들에 대해 Getter를 자동완성 시켜주는 어노테이션, 변수 이름 위에 적용시키면 해당 변수들만 적용 가능
@Setter             // 모든 변수들에 대해 Setter를 자동완성 시켜주는 어노테이션, 변수 이름 위에 적용시키면 해당 변수들만 적용 가능
public class Boards {
    @Id // PRIMARY KEY
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private int id;

    @Column(nullable = false)   // NOT NULL
    private String title;

    @Column(nullable = false)   // NOT NULL
    private String content;

    @Column(nullable = false)   // NOT NULL
    private int hits;
}
