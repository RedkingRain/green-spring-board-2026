package com.green.spring_board.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonPropertyOrder
public class BoardResponse {
    private int id;                         // Boarder Id
    private String title;                   // 제목
    private String content;                 // 내용
    private int hits;                       // 조회수
    private int likeCount;                  // 좋아요 수
    @JsonProperty("isLikedByMe")
    private boolean isLikedByMe;            // 본인이 좋아요를 눌렀는지 여부
    private int authorId;                   // 작성자 ID
    private String authorNickname;          // 작성자 닉네임
    private LocalDateTime createdDatetime;  // 게시글 생성 일시
    private LocalDateTime updatedDatetime;  // 게시글 수정 일시
}
