package com.green.spring_board.scheduler;

import com.green.spring_board.repository.AccessTokenBlackListRepository;
import com.green.spring_board.repository.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
@Slf4j  // 로깅 시스템을 활성화(로깅 추상화(Facade) 라이브러리)
public class TokenCleanupScheduler {
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenBlackListRepository accessTokenBlackListRepository;

    @Transactional
    // 매일 오전 3시 자동 실행(초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.warn("토큰 정리 스케쥴러가 실행됩니다. 실행 시작 시간 : {}", now);

        // 1. RefreshToken 테이블에서 만료 시간이 지난 리프레시 토큰들을 찾아 제거
        refreshTokenRepository.deleteAllByExpirationDatetimeBefore(now);

        // 2. AccessTokenBlackList 테이블에서 만료 시간이 지난 엑세스 토큰들을 찾아서 제거
        accessTokenBlackListRepository.deleteAllByExpirationDatetimeBefore(now);

        log.warn("토큰 정리 스케쥴러 작업이 성공적으로 마무리 되었습니다. 실행 종료 시간 : {}", LocalDateTime.now());
    }
}
