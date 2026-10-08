package com.green.spring_board.controller;

import com.green.spring_board.dto.ApiResponse;
import com.green.spring_board.dto.CommentCreateRequest;
import com.green.spring_board.dto.CommentResponse;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CommentController {
    private CommentService commentService;

    // 댓글 등록
    @PostMapping("/board/{id}/comment")
    public ResponseEntity<ApiResponse<Void>> createComment(
            HttpServletRequest httpServletRequest,
            @PathVariable int id,
            @Valid @RequestBody CommentCreateRequest commentCreateRequest
    ) {
        HttpSession session = httpServletRequest.getSession();
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }
        int userId = (int) session.getAttribute("userId");

        commentService.createComment(id, userId, commentCreateRequest);

        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 댓글 조회
    @GetMapping("/board/{id}/comment")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> readComment(
            @PathVariable int id
    ) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.readComments(id)));
    }

    // 댓글 수정


    // 댓글 삭제

}
