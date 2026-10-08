package com.green.spring_board.service;

import com.green.spring_board.dto.CommentCreateRequest;
import com.green.spring_board.dto.CommentResponse;
import com.green.spring_board.dto.CommentUpdateRequest;
import com.green.spring_board.entity.Board;
import com.green.spring_board.entity.Comment;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.*;
import com.green.spring_board.repository.BoardRepository;
import com.green.spring_board.repository.CommentRepository;
import com.green.spring_board.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CommentService {
    private CommentRepository commentRepository;
    private BoardRepository boardRepository;
    private UserRepository userRepository;

    // 댓글 등록
    public void createComment(
            int id,
            int userId,
            CommentCreateRequest commentCreateRequest
    ) {
        // 게시물 조회
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            // 요청한 게시글을 찾지 못한 경우
            throw new ResourceNotFoundException("요청한 게시글을 찾지 못했습니다.");
        }
        Board board = optionalBoard.get();

        // 댓글 작성자 정보 조회
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new UnauthenticatedException("로그인한 사용자를 찾을 수 없습니다.");
        }
        User user = optionalUser.get();

        // 데이터 생성
        Comment comment = new Comment();
        comment.setContent(commentCreateRequest.getContent());
        comment.setUser(user);
        comment.setBoard(board);

        commentRepository.save(comment);
    }

    // 댓글 조회
    public List<CommentResponse> readComments(int boardId) {
        // 게시물 조회
        if (!boardRepository.existsById(boardId)) {
            // 요청한 게시글을 찾지 못한 경우
            throw new ResourceNotFoundException("요청한 게시글을 찾지 못했습니다.");
        }

        List<Comment> comments = commentRepository.findByBoardId(boardId);
        List<CommentResponse> commentResponses = new ArrayList<>();

        for (Comment comment : comments) {
            commentResponses.add(
                    new CommentResponse(
                        comment.getId(),
                        comment.getUser().getNickname(),
                        comment.getContent(),
                        comment.getCreatedDatetime()
                    )
            );
        }

        return commentResponses;
    }

    // 댓글 수정
    public void updateComment(int commentId, CommentUpdateRequest commentUpdateRequest, int userId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isEmpty()) {
            throw new ResourceNotFoundException("요청한 댓글을 찾지 못했습니다.");
        }
        Comment comment = optionalComment.get();

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new UnauthenticatedException("로그인한 사용자를 찾을 수 없습니다.");
        }
        User user = optionalUser.get();

        if (comment.getUser().getId() != user.getId()) {
            throw new AuthorizationFailureException("수정할 권한이 없습니다.");
        }

        if (commentUpdateRequest.getContent() != null) {
            comment.setContent(commentUpdateRequest.getContent());
            commentRepository.save(comment);
        }
    }

    // 댓글 삭제
    public void deleteComment(int commentId, int userId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isEmpty()) {
            throw new ResourceNotFoundException("요청한 댓글을 찾지 못했습니다.");
        }
        Comment comment = optionalComment.get();

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new UnauthenticatedException("로그인한 사용자를 찾을 수 없습니다.");
        }
        User user = optionalUser.get();

        if (comment.getUser().getId() != user.getId()) {
            throw new AuthorizationFailureException("삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}
