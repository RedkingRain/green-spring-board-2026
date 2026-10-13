package com.green.spring_board.service;

import com.green.spring_board.dto.*;
import com.green.spring_board.entity.AccessTokenBlackList;
import com.green.spring_board.entity.RefreshToken;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.ResourceConflictException;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.global.UserState;
import com.green.spring_board.repository.AccessTokenBlackListRepository;
import com.green.spring_board.repository.RefreshTokenRepository;
import com.green.spring_board.repository.UserRepository;
import com.green.spring_board.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AccessTokenBlackListRepository accessTokenBlackListRepository;

    // 회원가입
    public void signup(SignupRequest signupRequest) {
        // 이메일과 비밀번호가 공백이 아닌지 확인
        // @Valid & @NotBlank 로 해당 체크 대체
//        if (signupRequest.getEmail().isBlank() || signupRequest.getPassword().isBlank()) {
//            throw new UserRequestException("Email or Password cannot be blank");
//        }

        // username이 사용 중인지 확인
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new ResourceConflictException("Username already exists");
        }

        // 이메일이 사용 중인지 확인
        if (userRepository.existsByUsername(signupRequest.getEmail())) {
            throw new ResourceConflictException("Email already exists");
        }

        // 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // db save
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(hashedPassword);
        user.setNickname(signupRequest.getNickname());
        user.setState(UserState.ACTIVE);
        userRepository.save(user);
    }

    // 로그인
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new UnauthenticatedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 1.이메일과 비밀번호가 공백이 아닌지 확인
        // @Valid & @NotBlank 로 해당 체크 대체
//        if (loginRequest.getEmail().isBlank() || loginRequest.getPassword().isBlank()) {
//            throw new UserRequestException("Email or Password cannot be blank");
//        }

        // 2. 이메일 존재하는건지 확인
        User user = userRepository.findByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new ResourceNotFoundException("User Not Found");
        }

        if (user.getState() == UserState.QUITTED) {
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }

        // 엑세스 토큰 발급
        String accessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getId());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        // 3-1. 해당 유저의 기존 리프레시 토큰(들) 삭제
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(user.getId());
        refreshTokenRepository.deleteAll(tokens);

        // 3-2. 해당 유저의 신규 리프레시 토큰 저장
        Date expiration = tokenProvider.getExpirationToken(refreshToken);
        LocalDateTime expirationDatetime = expiration.toInstant().atZone(
                ZoneId.systemDefault()
        ).toLocalDateTime();

        RefreshToken refreshTokenObj = new RefreshToken();
        refreshTokenObj.setUser(user);
        refreshTokenObj.setToken(refreshToken);
        refreshTokenObj.setExpirationDatetime(expirationDatetime);
        refreshTokenRepository.save(refreshTokenObj);

        // 4. 로그인 성공
        return new LoginResponse(accessToken, refreshToken);
    }

    // 로그아웃(토큰 삭제)
    public void logout(String accessToken) {
        // 1. 해당 사용자의 리프레시 토큰 무효화
        // - DB에서 해당 사용자의 리프레시 토큰을 지운다.
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(user.getId());
        refreshTokenRepository.deleteAll(tokens);

        // 2. 해당 사용자의 엑세스 토큰 무효화
        // - DB 내 블랙리스트 테이블에 해당 토큰을 추가한다.
        // - 만료 이후에는 블랙리스트 해제가 필요하므로 만료일자도 함께 넣는다.
        Date expiration = tokenProvider.getExpirationToken(accessToken);
        LocalDateTime convertedDateTime = expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        AccessTokenBlackList accessTokenBlackList = new AccessTokenBlackList();
        accessTokenBlackList.setToken(accessToken);
        accessTokenBlackList.setExpirationDatetime(convertedDateTime);
        accessTokenBlackListRepository.save(accessTokenBlackList);
    }

    // 엑세스 토큰 재발급
    public RefreshResponse refresh(@Valid RefreshRequest refreshRequest) {
        String userRefreshToken = refreshRequest.getRefreshToken();
        // 1. 리프레시 토큰 위/변조 여부 검증
        if (!tokenProvider.validateRefreshToken(userRefreshToken)) {
            // 검증 실패 예외처리
            throw new UnauthenticatedException("만료되었거나 유효하지 않는 리프레시 토큰입니다.");
        }

        // 2. DB 내 존재하는 리프레시 토큰인지 검증
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByToken(userRefreshToken);
        if (optionalRefreshToken.isEmpty()) {
            throw new UnauthenticatedException("유효하지 않는 리프레시 토큰입니다.");
        }

        // 3. 해당 리프레시 토큰이 만료되진 않았는지 검증
        // ! + isAfter = 만료 시간이 현재보다 이전이거나 같은 경우 (=현재보다 미래가 아닌 경우)
        // isBefore = 만료 시간이 현재보다 이전인 경우
        // 만료 시각과 현재 시각이 정확히 같을 때도 만료된 것으로 처리
        if (!optionalRefreshToken.get().getExpirationDatetime().isAfter(LocalDateTime.now())) {
            throw new UnauthenticatedException("만료된 리프레시 토큰입니다.");
        }

        // 4. 토큰 내 유저네임(subject)의 유저 정보가 유효한지 검증
        // 해당 유저가 존재하는지, 활성화 상태인지(탈퇴하지는 않았는지) 등등
        String userName = tokenProvider.getUsernameFromToken(userRefreshToken);
        User user = userRepository.findByUsername(userName);
        if (user == null ||  user.getState() != UserState.ACTIVE) {
            throw new UnauthenticatedException("인증할 수 없는 계정입니다.");
        }

        // 신규 엑세스 토큰 생성
        String accessToken = tokenProvider.generateAccessToken(userName, user.getId());
        return new RefreshResponse(accessToken);
    }

    // 접속자 정보 조회
    public MyInfoResponse getUserInfo(int userId) {
        // 1. 유저 아이디로 DB 조회함
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User Info Not Found!");
        }

        User user = optionalUser.get();

        if (user.getState() == UserState.QUITTED) {
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }
        // 2. DB에서 이 유저의 닉네임과 이메일을 받아옴.
        // 3. MyInfoResponse 객체를 생성하여 돌려줌
        return new MyInfoResponse(user.getEmail(), user.getNickname());
    }

    // 회원 정보 수정
    public void updateUserInfo(int userId, UserUpdateRequest userUpdateRequest) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User Info Not Found!");
        }
        User user = optionalUser.get();

        if (user.getState() == UserState.QUITTED) {
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }
        if (userRepository.existsByEmail(userUpdateRequest.getEmail())) {
            throw new ResourceConflictException("Email already exists");
        }
        if (userUpdateRequest.getEmail() != null && !userUpdateRequest.getEmail().isBlank()) {
            user.setEmail(userUpdateRequest.getEmail());
        }
        if (userUpdateRequest.getNickname() != null && !userUpdateRequest.getNickname().isBlank()) {
            user.setNickname(userUpdateRequest.getNickname());
        }

        userRepository.save(user);
    }

    // 회원 탈퇴
    public void deleteUser(int userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User Info Not Found!");
        }
        User user = optionalUser.get();

        if (user.getState() == UserState.QUITTED) {
            throw new ResourceNotFoundException("탈퇴된 회원입니다.");
        }

//        userRepository.deleteById(userId);
        user.setState(UserState.QUITTED);
        userRepository.save(user);
    }

}
