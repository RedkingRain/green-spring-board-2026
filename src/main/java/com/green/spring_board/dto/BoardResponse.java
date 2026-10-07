package com.green.spring_board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BoardResponse {
    private int id;                         // Boarder Id
    private String title;                   // 제목
    private String content;                 // 내용
    private int hits;                       // 조회수
    private int likeCount;                  // 좋아요 수
    private int authorId;                   // 작성자 ID
    private String authorNickname;          // 작성자 닉네임
    private LocalDateTime createdDatetime;  // 게시글 생성 일시
    private LocalDateTime updatedDatetime;  // 게시글 수정 일시
}
