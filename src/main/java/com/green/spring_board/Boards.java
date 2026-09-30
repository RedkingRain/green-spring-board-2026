package com.green.spring_board;

import jakarta.persistence.*;

@Entity
@Table(name = "boards") // 실제 테이블 명 기입
public class Boards {
    @Id // PRIMARY KEY
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private int id;

    @Column(nullable = false)   // NOT NULL
    private String title;

    @Column(nullable = false)   // NOT NULL
    private String content;

    // 생성자
    public Boards() {}

    public Boards(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
