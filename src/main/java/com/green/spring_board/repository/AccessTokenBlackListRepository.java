package com.green.spring_board.repository;

import com.green.spring_board.entity.AccessTokenBlackList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessTokenBlackListRepository extends JpaRepository<AccessTokenBlackList, Integer> {
    Optional<AccessTokenBlackList> findByToken(String token);
}
