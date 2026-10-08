package com.green.spring_board.entity;

import com.green.spring_board.global.UserState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {
    @Id // PRIMARY KEY
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private int id;

    @Column(nullable = false)   // NOT NULL
    private String nickname;

    @Column(nullable = false, unique = true)   // NOT NULL
    private String email;

    @Column(nullable = false)   // NOT NULL
    private String password;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdDatetime;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedDatetime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserState state;
}
