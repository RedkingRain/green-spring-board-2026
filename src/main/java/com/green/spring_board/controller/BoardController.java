package com.green.spring_board.controller;

import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UserRequestException;
import com.green.spring_board.dto.BoardCreateRequest;
import com.green.spring_board.entity.Board;
import com.green.spring_board.service.BoardService;
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
    public ResponseEntity<List<Board>> getBoards(){
        return ResponseEntity.ok(boardService.getAllBoards());  // 200 & List<Boards> Return
    }

    // 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardDetail(@PathVariable int id){
        try {
            Board board = boardService.getBoard(id);
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
    public ResponseEntity<Void> createBoard(@RequestBody BoardCreateRequest boardCreateRequest) {
        try {
            int newBoardId = boardService.createBoard(boardCreateRequest);
            URI location = URI.create("/api/board/" + newBoardId);
            return ResponseEntity.created(location).build();  // 201 Return
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
            @RequestBody BoardCreateRequest boardCreateRequest
    ) {
        try {
            boardService.updateBoard(id, boardCreateRequest);
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
