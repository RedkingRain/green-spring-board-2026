package com.green.spring_board.controller;

import com.green.spring_board.dto.BoardCreateRequest;
import com.green.spring_board.entity.Boards;
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
    public ResponseEntity<List<Boards>> getBoards(){
        return ResponseEntity.ok(boardService.getAllBoards());  // 200 & List<Boards> Return
    }

    // 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Boards> getBoardDetail(@PathVariable int id){
        Boards board = boardService.getBoard(id);
        if (board == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(board);  // 200 & Boards Return
    }

    // 삽입
    @PostMapping
    public ResponseEntity<Void> createBoard(@RequestBody BoardCreateRequest boardCreateRequest) {
        int newBoardId = boardService.createBoard(boardCreateRequest);

        if (newBoardId == -1) {
            return ResponseEntity.badRequest().build();
        }

        URI location = URI.create("/api/board/" + newBoardId);

        return ResponseEntity.created(location).build();  // 201 Return
    }

    // 수정
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateBoard(
            @PathVariable int id,
            @RequestBody BoardCreateRequest boardCreateRequest
    ) {
        int code = boardService.updateBoard(id, boardCreateRequest);
        if (code == -1) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();  // 200 Return
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable int id) {
        int code = boardService.deleteBoard(id);
        if (code == -1) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();  // 204 Return
    }
}
