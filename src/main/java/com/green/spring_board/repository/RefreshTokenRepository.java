package com.green.spring_board.repository;

import com.green.spring_board.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    void deleteByUserId(int userId);
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserId(int userId);
    // expirationDateTime이 매개변수의 시간보다 이전인 모든 데이터를 삭제한다.
    void deleteAllByExpirationDatetimeBefore(LocalDateTime expirationDatetime);
}
