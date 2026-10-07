package com.green.spring_board.controller;

import com.green.spring_board.dto.ApiResponse;
import com.green.spring_board.dto.BoardResponse;
import com.green.spring_board.dto.BoardUpdateRequest;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.dto.BoardCreateRequest;
import com.green.spring_board.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getBoards(){
//        return ResponseEntity.ok(boardService.getAllBoards());  // 200 & List<Boards> Return
        return ResponseEntity.ok(ApiResponse.ok(boardService.getAllBoards()));
    }

    // 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardResponse>> getBoardDetail(@PathVariable int id){
        BoardResponse board = boardService.getBoard(id);
//        return ResponseEntity.ok(board);  // 200 & Boards Return
        return ResponseEntity.ok(ApiResponse.ok(board));
    }

    // 본인 게시글 조회
    @GetMapping("/my_boards")
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getMyBoard(HttpServletRequest httpServletRequest){
        HttpSession session = httpServletRequest.getSession();

        int userId = (int) session.getAttribute("userId");
        List<BoardResponse> boardResponseList = boardService.getMyBoards(userId);

        return ResponseEntity.ok(ApiResponse.ok(boardResponseList));
    }

    // 삽입
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBoard(
            @Valid @RequestBody BoardCreateRequest boardCreateRequest,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        // 2. 세션에서 유저 아이디 뽑아옴
        int userId = (int) session.getAttribute("userId");
        int newBoardId = boardService.createBoard(boardCreateRequest, userId);
        URI location = URI.create("/api/board/" + newBoardId);

//        return ResponseEntity.created(location).build();  // 201 Return
        return ResponseEntity.created(location).body(ApiResponse.ok());
    }

    // 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateBoard(
            @PathVariable int id,
            @Valid @RequestBody BoardUpdateRequest boardUpdateRequest,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        int userId = (int) session.getAttribute("userId");
        boardService.updateBoard(id, boardUpdateRequest, userId);
//        return ResponseEntity.ok().build();  // 200 Return
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable int id,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UnauthenticatedException("로그인이 필요합니다.");
        }

        // 둘 중 어느 방법을 쓸 지는 속한 팀, 조직 컨벤션 따르기

        // 삭제 성공 시 응답 방법 1.
        // 200 + ApiResponse<Void>

        // 삭제 성공 시 응답 방법 2.
        // 204(No Content) + No Body

        int userId = (int) session.getAttribute("userId");
        boardService.deleteBoard(id, userId);
//        return ResponseEntity.noContent().build();  // 204 Return
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok());
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
