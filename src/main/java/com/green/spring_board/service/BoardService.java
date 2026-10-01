package com.green.spring_board.service;

import com.green.spring_board.dto.BoardCreateRequest;
import com.green.spring_board.repository.BoardRepository;
import com.green.spring_board.entity.Boards;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BoardService {
    private BoardRepository boardRepository;

    // 전체 조회
    public List<Boards> getAllBoards() {
        return boardRepository.findAll();  // 200 & List<Boards> Return
    }

    // 상세 조회
    public Boards getBoard(int id) {
        Optional<Boards> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            // 요청한 게시글을 찾지 못한 경우
            return null;
        }

        Boards board = optionalBoard.get();

        board.setHits(board.getHits() + 1);
        boardRepository.save(board);
        return board;
    }

    // 삽입
    public int createBoard(BoardCreateRequest boardCreateRequest) {
        if (boardCreateRequest.getTitle() == null || boardCreateRequest.getTitle().isBlank()) {
            return -1;
        }
        if (boardCreateRequest.getContent() == null || boardCreateRequest.getContent().isBlank()) {
            return -1;
        }

        Boards board = new Boards();
        board.setTitle(boardCreateRequest.getTitle());
        board.setContent(boardCreateRequest.getContent());

        Boards saveBoard = boardRepository.save(board);
        return saveBoard.getId();
    }

    // 수정
    public int updateBoard(int id, BoardCreateRequest boardCreateRequest) {
        Optional<Boards> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            return -1;
        }

        Boards board = optionalBoard.get();

        if (
                (boardCreateRequest.getTitle() == null || boardCreateRequest.getTitle().isBlank()) ||
                (boardCreateRequest.getContent() == null || boardCreateRequest.getContent().isBlank())
        ) {
            return -1;
        } else {
            board.setTitle(boardCreateRequest.getTitle());
            board.setContent(boardCreateRequest.getContent());
        }

        boardRepository.save(board);
        return 1;
    }

    // 삭제
    public int deleteBoard(int id) {
        boolean isExist = boardRepository.existsById(id);

        if (!isExist) {
            return -1;
        }

        boardRepository.deleteById(id);
        return 1;
    }
}
