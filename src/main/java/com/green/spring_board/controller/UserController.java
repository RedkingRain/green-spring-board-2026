package com.green.spring_board.controller;

import com.green.spring_board.dto.LoginRequest;
import com.green.spring_board.dto.MyInfoResponse;
import com.green.spring_board.dto.SignupRequest;
import com.green.spring_board.exceptions.ResourceConflictException;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.exceptions.UserRequestException;
import com.green.spring_board.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest signupRequest) {
        try {
            userService.signup(signupRequest);
            return ResponseEntity.ok().build();
        } catch (ResourceConflictException e) {
            // DB에 중복된 값이 이미 있을 때
            return ResponseEntity.status(409).build();
        } catch (UserRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpServletRequest
    ) {
        try {
            int userId = userService.login(loginRequest);

            // 세션 생성 작업
            // 사용자의 요청값에서 세션 존재하는지 확인 후 세션이 존재하면 세션 들고옴
            // 세션이 없다면 새로운 세션 생성
            HttpSession session = httpServletRequest.getSession();
            // 사용자가 잘못된 세션ID를 올려줄 수 있으니 세션ID 초기화 진행(로그인은 세션이 없을 경우가 대부분이기 때문에)
            httpServletRequest.changeSessionId();
            session.setAttribute("userId", userId); // 세션 값에 userId 항목 추가(세션이 어떤 계정에 대한 세션인지 알기 위해)

            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthenticatedException e) {
            return ResponseEntity.status(401).build();
        } catch (UserRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<MyInfoResponse> getCurrentUser(HttpServletRequest request) {
        // "내" 정보 조회하기
        try {
            // 1. 이사람의 세션을 가져옴
            /*
                "me"는 회원 전용 서비스다.
                이 사람의 세션이 없으면, 새로 만들어주는게 아니라 내쫓아야 함
                그래서 세션이 없다고 세션을 만들지 않도록 getSession 안에 (false) 옵션을 추가하여 null을 반환한다.
            */
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                return ResponseEntity.status(401).build();
            }

            // 2. 세션에서 유저 아이디 뽑아옴
            int userId = Integer.parseInt(session.getAttribute("userId").toString());

            // 3. 유저 아이디로 DB 조회함
            // 4. DB에서 이 유저의 닉네임과 이메일을 받아옴.
            MyInfoResponse myInfoResponse = userService.getUserInfo(userId);

            // 5. 돌려줌
            return ResponseEntity.ok().body(myInfoResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                return ResponseEntity.status(401).build();
            }

            session.invalidate(); // 세션 만료 처리
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<Void> updateUserInfo(
            HttpServletRequest request,
            @RequestBody MyInfoResponse myInfoResponse
    ) {
        // 이메일 닉네임 업데이트
        // 현재 유저를 가져와서, 해당 유저 정보를
        // 사용자가 올린 요청으로 덮어씌운다.
        // 보드 했던것처럼 null이면 수정하지 않기!
        try {
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                return ResponseEntity.status(401).build();
            }

            int userId = (int) session.getAttribute("userId");
            userService.updateUserInfo(userId, myInfoResponse);

            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ResourceConflictException e) {
            return ResponseEntity.status(409).build();
        } catch (UserRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                return ResponseEntity.status(401).build();
            }

            int userId = (int) session.getAttribute("userId");
            // 1. DB 삭제
            userService.deleteUser(userId);
            // 2. 세션 비활성화
            session.invalidate();

            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
