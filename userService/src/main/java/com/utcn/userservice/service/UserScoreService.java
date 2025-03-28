package com.utcn.userservice.service;

import com.utcn.userservice.entity.ScoreChange;
import com.utcn.userservice.entity.User;
import com.utcn.userservice.repository.ScoreChangeRepository;
import com.utcn.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserScoreService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScoreChangeRepository scoreChangeRepository;

    @Transactional
    public float adjustUserScore(Integer userId, float adjustment, String reason, Integer referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            
        if (user.getScore() == null) {
            user.setScore(0.0f);
        }
        user.setScore(user.getScore() + adjustment);
        userRepository.save(user);
        
        ScoreChange scoreChange = new ScoreChange();
        scoreChange.setUserId(userId);
        scoreChange.setScoreChange(adjustment);
        scoreChange.setReason(reason);
        scoreChange.setReferenceId(referenceId);
        scoreChangeRepository.save(scoreChange);
        
        return user.getScore();
    }

    public float getUserScore(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        return user.getScore() != null ? user.getScore() : 0.0f;
    }

    public List<ScoreChange> getUserScoreHistory(Integer userId) {
        return scoreChangeRepository.findByUserId(userId);
    }
} 