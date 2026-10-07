package com.green.spring_board.service;

import com.green.spring_board.dto.BoardResponse;
import com.green.spring_board.dto.BoardUpdateRequest;
import com.green.spring_board.entity.Like;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.AuthorizationFailureException;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.dto.BoardCreateRequest;
import com.green.spring_board.repository.BoardRepository;
import com.green.spring_board.entity.Board;
import com.green.spring_board.repository.LikeRepository;
import com.green.spring_board.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    private LikeRepository likeRepository;

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
                            board.getLikeCount(),
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

        return new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getHits(),
                board.getLikeCount(),
                board.getUser().getId(),
                board.getUser().getNickname(),
                board.getCreatedDatetime(),
                board.getUpdatedDatetime()
        );
    }

    // 본인 게시글 조회
    public List<BoardResponse> getMyBoards(int userId) {
        List<Board> boardList = boardRepository.findByUserId(userId);

        if (boardList.isEmpty()) {
            throw new ResourceNotFoundException("작성된 게시물이 없습니다.");
        }

        List<BoardResponse> boardResponsesList = new ArrayList<>();

        for (Board board : boardList) {
            boardResponsesList.add(
                    new BoardResponse(
                            board.getId(),
                            board.getTitle(),
                            board.getContent(),
                            board.getHits(),
                            board.getLikeCount(),
                            board.getUser().getId(),
                            board.getUser().getNickname(),
                            board.getCreatedDatetime(),
                            board.getUpdatedDatetime()
                    )
            );
        }

        return boardResponsesList;
    }

    // 삽입
    public int createBoard(BoardCreateRequest boardCreateRequest, Integer userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new UnauthenticatedException("로그인한 사용자를 찾을 수 없습니다.");
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
    public void updateBoard(int id, BoardUpdateRequest boardUpdateRequest, int userId) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            // 게시글을 못 찾은 경우
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }

        Board board = optionalBoard.get();

        // 작성자와 요청자 동일 여부 확인
        if (board.getUser().getId() != userId) {
            throw new AuthorizationFailureException("게시글 작업 권한이 없습니다.");
        }

        if (boardUpdateRequest.getTitle() != null && !boardUpdateRequest.getTitle().isBlank()) {
            board.setTitle(boardUpdateRequest.getTitle());
        }
        if (boardUpdateRequest.getContent() != null && !boardUpdateRequest.getContent().isBlank()) {
            board.setContent(boardUpdateRequest.getContent());
        }

        boardRepository.save(board);
    }

    // 삭제
    public void deleteBoard(int id, int userId) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            // 게시글을 못 찾은 경우
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }

        Board board = optionalBoard.get();

        // 작성자와 요청자 동일 여부 확인
        if (board.getUser().getId() != userId) {
            throw new AuthorizationFailureException("게시글 작업 권한이 없습니다.");
        }

        boardRepository.deleteById(id);
    }

    // 게시글 좋아요
    public void pressLike(int id, int userId) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isEmpty()) {
            // 게시글을 못 찾은 경우
            throw new ResourceNotFoundException("존재하지 않는 게시물 입니다.");
        }
        Board board = optionalBoard.get();

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("존재하지 않는 유저 입니다.");
        }
        User user = optionalUser.get();

        // 1. 이 유저와 보드로 동일한 좋아요가 있는지 확인
        Optional<Like> optionalLike = likeRepository.findByUserIdAndBoardId(userId, id);
        if (optionalLike.isEmpty()) {  // 2. 없으면 추가
            Like like = new Like();
            like.setBoard(board);
            like.setUser(user);
            likeRepository.save(like);

            board.setLikeCount(board.getLikeCount() + 1);
            boardRepository.save(board);
        }
        else {  // 3. 있으면 삭제
            Like like = optionalLike.get();
            likeRepository.deleteById(like.getId());

            board.setLikeCount(board.getLikeCount() - 1);
            boardRepository.save(board);
        }
    }
}
