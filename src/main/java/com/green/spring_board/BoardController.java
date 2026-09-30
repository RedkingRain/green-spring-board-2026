package com.green.spring_board;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board")
@AllArgsConstructor
public class BoardController {
    private BoardRepository boardRepository;

    // 전체 조회
    @GetMapping // 여기에는 경로를 추가 하지 않는 이유는 REST API URL 네이밍 규칙 위반(URL + HTTP + Method 조합으로 결과를 예측가능 해야함)
    public List<Boards> getBoards(){
        return boardRepository.findAll();
    }

    // 상세 조회
    @GetMapping("/{id}")
    public Boards getBoardDetail(@PathVariable int id){
        Boards board = boardRepository.findById(id).get();
        board.setHits(board.getHits() + 1);
        boardRepository.save(board);

        return board;
    }

    // 삽입
    @PostMapping
    public void createBoard(@RequestBody BoardCreateRequest boardCreateRequest) {
        Boards board = new Boards();
        board.setTitle(boardCreateRequest.getTitle());
        board.setContent(boardCreateRequest.getContent());

        boardRepository.save(board);
    }

    // 수정
    @PatchMapping("/{id}")
    public void updateBoard(
            @PathVariable int id,
            @RequestBody BoardCreateRequest boardCreateRequest
    ) {
        Boards board = boardRepository.findById(id).get();

        if (boardCreateRequest.getTitle() != null){
            board.setTitle(boardCreateRequest.getTitle());
        }
        if (boardCreateRequest.getContent() != null){
            board.setContent(boardCreateRequest.getContent());
        }
        if (boardCreateRequest.getHits() > 0){
            board.setHits(boardCreateRequest.getHits());
        }

        boardRepository.save(board);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public void deleteBoard(@PathVariable int id) {
        boardRepository.deleteById(id);
    }
}
