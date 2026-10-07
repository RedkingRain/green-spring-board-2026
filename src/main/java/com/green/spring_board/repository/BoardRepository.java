package com.green.spring_board.repository;

import com.green.spring_board.entity.Board;
import com.green.spring_board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer>{
    List<Board> findByUserId(int userId);
}
