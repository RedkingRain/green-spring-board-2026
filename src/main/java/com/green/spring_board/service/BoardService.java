package com.green.spring_board.service;

import com.green.spring_board.dto.BoardResponse;
import com.green.spring_board.dto.BoardUpdateRequest;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.exceptions.UserRequestException;
import com.green.spring_board.dto.BoardCreateRequest;
import com.green.spring_board.repository.BoardRepository;
import com.green.spring_board.entity.Board;
import com.green.spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BoardService {
    private BoardRepository boardRepository;
    private UserRepository userRepository;

    // 전체 조회
    public List<BoardResponse> getAllBoards() {
        List<Board> boardList = boardRepository.findAll();

        // List<board> -> List<BoardResponse> 형태로 반환
        // 1. List<BoardResponse> 형태의 빈 리스트 생성
        List<BoardResponse> boardResponsesList = new ArrayList<>();

        // 2. Board 개수만큼 반복하며 new BoardResponse 생성
        for (Board board : boardList) {
            // 3. 1번에서 만든 리스트에 추가
            boardResponsesList.add(
                    new BoardResponse(
                            board.getId(),
                            board.getTitle(),
                            board.getContent(),
                            board.getHits(),
                            board.getUser().getId(),
                            board.getUser().getNickname(),
                            board.getCreatedDatetime(),
                            board.getUpdatedDatetime()
                    )
            );
        }

        // 4. 반환
        return boardResponsesList;
    }

    // 상세 조회
    public BoardResponse getBoard(int id) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            // 요청한 게시글을 찾지 못한 경우
            throw new ResourceNotFoundException("요청한 게시글을 찾지 못했습니다.");
        }

        Board board = optionalBoard.get();
        board.setHits(board.getHits() + 1);
        boardRepository.save(board);

        BoardResponse boardResponse = new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getHits(),
                board.getUser().getId(),
                board.getUser().getNickname(),
                board.getCreatedDatetime(),
                board.getUpdatedDatetime()
        );

        return boardResponse;
    }

    // 삽입
    public int createBoard(BoardCreateRequest boardCreateRequest, Integer userId) {
        if (boardCreateRequest.getTitle() == null || boardCreateRequest.getTitle().isBlank()) {
            // 사용자가 값을 잘못 입력한 경우
            throw new UserRequestException("잘못된 입력값 입니다.");
        }
        if (boardCreateRequest.getContent() == null || boardCreateRequest.getContent().isBlank()) {
            // 사용자가 값을 잘못 입력한 경우
            throw new UserRequestException("잘못된 입력값 입니다.");
        }

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new UnauthenticatedException("로그인한 사용자르 찾을 수 없습니다.");
        }

        User user = optionalUser.get();

        Board board = new Board();
        board.setTitle(boardCreateRequest.getTitle());
        board.setContent(boardCreateRequest.getContent());
        board.setUser(user);

        Board saveBoard = boardRepository.save(board);
        return saveBoard.getId();
    }

    // 수정
    public void updateBoard(int id, BoardUpdateRequest boardUpdateRequest) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            // 게시글을 못 찾은 경우
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }

        Board board = optionalBoard.get();

        if (
                (boardUpdateRequest.getTitle() == null || boardUpdateRequest.getTitle().isBlank()) ||
                (boardUpdateRequest.getContent() == null || boardUpdateRequest.getContent().isBlank())
        ) {
            // 사용자가 값을 잘못 입력한 경우
            throw new UserRequestException("잘못된 입력값 입니다.");
        }

        board.setTitle(boardUpdateRequest.getTitle());
        board.setContent(boardUpdateRequest.getContent());

        boardRepository.save(board);
    }

    // 삭제
    public void deleteBoard(int id) {
        boolean isExist = boardRepository.existsById(id);

        if (!isExist) {
            // 게시글을 못 찾은 경우
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }

        boardRepository.deleteById(id);
    }
}
