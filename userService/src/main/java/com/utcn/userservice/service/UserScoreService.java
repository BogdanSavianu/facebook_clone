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

    /**
     * Adjusts a user's score and logs the change
     * 
     * @param userId User ID to adjust score for
     * @param adjustment Point value to add or subtract
     * @param reason Description of why the score is being adjusted
     * @param referenceId ID of the post, comment, or vote that caused the change
     * @return Updated user score
     */
    @Transactional
    public float adjustUserScore(Integer userId, float adjustment, String reason, Integer referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            
        // Update user score
        if (user.getScore() == null) {
            user.setScore(0.0f);
        }
        user.setScore(user.getScore() + adjustment);
        userRepository.save(user);
        
        // Log the score change
        ScoreChange scoreChange = new ScoreChange();
        scoreChange.setUserId(userId);
        scoreChange.setScoreChange(adjustment);
        scoreChange.setReason(reason);
        scoreChange.setReferenceId(referenceId);
        scoreChangeRepository.save(scoreChange);
        
        return user.getScore();
    }
    
    /**
     * Retrieves a user's current score
     * 
     * @param userId User ID
     * @return The user's current score
     */
    public float getUserScore(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        return user.getScore() != null ? user.getScore() : 0.0f;
    }
    
    /**
     * Gets the history of a user's score changes
     * 
     * @param userId User ID
     * @return List of score change records
     */
    public List<ScoreChange> getUserScoreHistory(Integer userId) {
        return scoreChangeRepository.findByUserId(userId);
    }
} 