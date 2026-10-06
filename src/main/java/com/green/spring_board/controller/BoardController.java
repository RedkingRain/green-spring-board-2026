package com.green.spring_board.controller;

import com.green.spring_board.dto.BoardResponse;
import com.green.spring_board.dto.BoardUpdateRequest;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.exceptions.UserRequestException;
import com.green.spring_board.dto.BoardCreateRequest;
import com.green.spring_board.entity.Board;
import com.green.spring_board.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/board")
@AllArgsConstructor
public class BoardController {
    private final BoardService boardService;

    // 전체 조회
    @GetMapping // 여기에는 경로를 추가 하지 않는 이유는 REST API URL 네이밍 규칙 위반(URL + HTTP + Method 조합으로 결과를 예측가능 해야함)
    public ResponseEntity<List<BoardResponse>> getBoards(){
        return ResponseEntity.ok(boardService.getAllBoards());  // 200 & List<Boards> Return
    }

    // 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> getBoardDetail(@PathVariable int id){
        try {
            BoardResponse board = boardService.getBoard(id);
            return ResponseEntity.ok(board);  // 200 & Boards Return
        } catch (ResourceNotFoundException e) {
            // 게시글을 못 찾았을 때 (404)
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // 위에도 아니면, 무조건 Java 아니면 DB 에러로 서버 에러 (500)
            return ResponseEntity.internalServerError().build();
        }
    }

    // 삽입
    @PostMapping
    public ResponseEntity<Void> createBoard(
            @Valid @RequestBody BoardCreateRequest boardCreateRequest,
            HttpServletRequest httpServletRequest
    ) {
        try {
            HttpSession session = httpServletRequest.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                return ResponseEntity.status(401).build();
            }
            // 2. 세션에서 유저 아이디 뽑아옴
            int userId = (int) session.getAttribute("userId");
            int newBoardId = boardService.createBoard(boardCreateRequest, userId);
            URI location = URI.create("/api/board/" + newBoardId);
            return ResponseEntity.created(location).build();  // 201 Return
        } catch (UnauthenticatedException e) {
            return ResponseEntity.status(401).build();
        } catch (UserRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 수정
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateBoard(
            @PathVariable int id,
            @Valid @RequestBody BoardUpdateRequest boardUpdateRequest
    ) {
        try {
            boardService.updateBoard(id, boardUpdateRequest);
            return ResponseEntity.ok().build();  // 200 Return
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UserRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable int id) {
        try {
            boardService.deleteBoard(id);
            return ResponseEntity.noContent().build();  // 204 Return
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
