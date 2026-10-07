package com.green.spring_board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "boards") // 실제 테이블 명 기입
// lombok
@AllArgsConstructor // 모든 변수를 사용하는 생성자를 자동완성 시켜주는 어노테이션
@NoArgsConstructor  // 어떠한 변수도 사용하지 않는 기본 생성자를 자동완성 시켜주는 어노테이션
@Getter             // 모든 변수들에 대해 Getter를 자동완성 시켜주는 어노테이션, 변수 이름 위에 적용시키면 해당 변수들만 적용 가능
@Setter             // 모든 변수들에 대해 Setter를 자동완성 시켜주는 어노테이션, 변수 이름 위에 적용시키면 해당 변수들만 적용 가능
public class Board {
    @Id // PRIMARY KEY
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private int id;

    @Column(nullable = false)   // NOT NULL
    private String title;

    @Column(nullable = false)   // NOT NULL
    private String content;

    @Column(nullable = false)   // NOT NULL
    private int hits;

//    @CreatedDate  : db에 해당 컬럼이 defualt 설정이 안되어있다면, jpa가 생성일자를 자동으로 채워 넣는다. (단, 데이터 값이 있는 경우 제외)
//    @LastModifiedDate : db에 해당 컬럼이 defualt 설정이 안되어있다면, jpa가 수정일자를 자동으로 채워 넣는다.
    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdDatetime;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private int likeCount;
}
