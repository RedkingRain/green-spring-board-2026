package com.green.spring_board;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.stream.Location;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/board")
@AllArgsConstructor
public class BoardController {
    private BoardRepository boardRepository;

    // 전체 조회
    @GetMapping // 여기에는 경로를 추가 하지 않는 이유는 REST API URL 네이밍 규칙 위반(URL + HTTP + Method 조합으로 결과를 예측가능 해야함)
    public ResponseEntity<List<Boards>> getBoards(){
        return ResponseEntity.ok(boardRepository.findAll());  // 200 & List<Boards> Return
//        return boardRepository.findAll();
    }

    // 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Boards> getBoardDetail(@PathVariable int id){
        Optional<Boards> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            // 요청한 게시글을 찾지 못한 경우
            return ResponseEntity.notFound().build();   // 404 Return
        }

        Boards board = optionalBoard.get();

        board.setHits(board.getHits() + 1);
        boardRepository.save(board);

        return ResponseEntity.ok(board);  // 200 & Boards Return
    }

    // 삽입
    @PostMapping
    public ResponseEntity<Boards> createBoard(@RequestBody BoardCreateRequest boardCreateRequest) {
        if (boardCreateRequest.getTitle() == null || boardCreateRequest.getTitle().isBlank()) {
            return ResponseEntity.badRequest().build(); // 400 Return
        }
        if (boardCreateRequest.getContent() == null || boardCreateRequest.getContent().isBlank()) {
            return ResponseEntity.badRequest().build(); // 400 Return
        }

        Boards board = new Boards();
        board.setTitle(boardCreateRequest.getTitle());
        board.setContent(boardCreateRequest.getContent());

        Boards saveBoard = boardRepository.save(board);
        int newBoardId = saveBoard.getId();
        URI location = URI.create("/api/board/" + newBoardId);

        return ResponseEntity.created(location).body(board);  // 201 Return
    }

    // 수정
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateBoard(
            @PathVariable int id,
            @RequestBody BoardCreateRequest boardCreateRequest
    ) {
        Optional<Boards> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Boards board = optionalBoard.get();

        if (
                (boardCreateRequest.getTitle() == null || boardCreateRequest.getTitle().isBlank()) ||
                (boardCreateRequest.getContent() == null || boardCreateRequest.getContent().isBlank()) ||
                (boardCreateRequest.getHits() <= 0)
        ) {
            return ResponseEntity.badRequest().build();
        } else {
            board.setTitle(boardCreateRequest.getTitle());
            board.setContent(boardCreateRequest.getContent());
            board.setHits(boardCreateRequest.getHits());
        }

        boardRepository.save(board);
        return ResponseEntity.ok().build();  // 200 Return
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable int id) {
        boolean isExist = boardRepository.existsById(id);

        if (!isExist) {
            return ResponseEntity.notFound().build();
        }

        boardRepository.deleteById(id);
        return ResponseEntity.noContent().build();  // 204 Return
    }
}
