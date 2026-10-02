package com.green.spring_board.service;

import com.green.spring_board.dto.LoginRequest;
import com.green.spring_board.dto.MyInfoResponse;
import com.green.spring_board.dto.SignupRequest;
import com.green.spring_board.entity.User;
import com.green.spring_board.exceptions.ResourceConflictException;
import com.green.spring_board.exceptions.ResourceNotFoundException;
import com.green.spring_board.exceptions.UnauthenticatedException;
import com.green.spring_board.exceptions.UserRequestException;
import com.green.spring_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 회원가입
    public void signup(SignupRequest signupRequest) {
        // 이메일과 비밀번호가 공백이 아닌지 확인
        if (signupRequest.getEmail().isBlank() || signupRequest.getPassword().isBlank()) {
            throw new UserRequestException("Email or Password cannot be blank");
        }

        // 이메일이 사용 중인지 확인
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ResourceConflictException("Email already exists");
        }

        // 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // db save
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(hashedPassword);
        user.setNickname(signupRequest.getNickname());
        userRepository.save(user);
    }

    // 로그인
    public int login(LoginRequest loginRequest) {
        // 1.이메일과 비밀번호가 공백이 아닌지 확인
        if (loginRequest.getEmail().isBlank() || loginRequest.getPassword().isBlank()) {
            throw new UserRequestException("Email or Password cannot be blank");
        }

        // 2. 이메일 존재하는건지 확인
        Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User Not Found");
        }

        User user = optionalUser.get();

        // 3. 비밀번호가 올바른지 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthenticatedException("Wrong password");
        }

        // 4. 로그인 성공
        return user.getId();
    }

    // 접속자 정보 조회
    public MyInfoResponse getUserInfo(int userId) {
        // 1. 유저 아이디로 DB 조회함
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User Info Not Found!");
        }

        User user = optionalUser.get();

        // 2. DB에서 이 유저의 닉네임과 이메일을 받아옴.
        // 3. MyInfoResponse 객체를 생성하여 돌려줌
        return new MyInfoResponse(user.getEmail(), user.getNickname());
    }

    // 회원 정보 수정
    public void updateUserInfo(int userId, MyInfoResponse myInfoResponse) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User Info Not Found!");
        }

        if (userRepository.existsByEmail(myInfoResponse.getEmail())) {
            throw new ResourceConflictException("Email already exists");
        }

        User user = optionalUser.get();

        if (
                (myInfoResponse.getEmail() == null || myInfoResponse.getEmail().isBlank())
             || (myInfoResponse.getNickname() == null || myInfoResponse.getNickname().isBlank())
        ){
            throw new UserRequestException("잘못된 입력값 입니다.");
        }

        user.setEmail(myInfoResponse.getEmail());
        user.setNickname(myInfoResponse.getNickname());

        userRepository.save(user);
    }

    // 회원 탈퇴
    public void deleteUser(int userId) {
        boolean isExist = userRepository.existsById(userId);

        if (!isExist) {
            throw new ResourceNotFoundException("사용자를 찾을 수 없습니다.");
        }

        userRepository.deleteById(userId);
    }
}
