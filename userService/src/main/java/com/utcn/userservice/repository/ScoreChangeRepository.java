package com.utcn.userservice.repository;

import com.utcn.userservice.entity.ScoreChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreChangeRepository extends JpaRepository<ScoreChange, Integer> {
    List<ScoreChange> findByUserId(Integer userId);
} 