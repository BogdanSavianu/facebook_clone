package com.utcn.userservice.controller;

import com.utcn.userservice.entity.ScoreChange;
import com.utcn.userservice.service.UserScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/scores")
public class UserScoreController {
    
    @Autowired
    private UserScoreService userScoreService;
    
    /**
     * Endpoint to adjust a user's score
     */
    @PostMapping("/{userId}/adjust")
    public ResponseEntity<Float> adjustUserScore(
            @PathVariable Integer userId,
            @RequestParam Float adjustment,
            @RequestParam String reason,
            @RequestParam(required = false) Integer referenceId) {
            
        float updatedScore = userScoreService.adjustUserScore(userId, adjustment, reason, referenceId);
        return ResponseEntity.ok(updatedScore);
    }
    
    /**
     * Endpoint to get a user's current score
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Float> getUserScore(@PathVariable Integer userId) {
        float score = userScoreService.getUserScore(userId);
        return ResponseEntity.ok(score);
    }
    
    /**
     * Endpoint to get a user's score history
     */
    @GetMapping("/{userId}/history")
    public ResponseEntity<List<ScoreChange>> getUserScoreHistory(@PathVariable Integer userId) {
        List<ScoreChange> history = userScoreService.getUserScoreHistory(userId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Endpoint to get scores for multiple users
     */
    @GetMapping("/batch")
    public ResponseEntity<Map<Integer, Float>> getBatchUserScores(@RequestParam List<Integer> userIds) {
        Map<Integer, Float> scores = userIds.stream()
                .collect(java.util.stream.Collectors.toMap(
                    userId -> userId,
                    userId -> userScoreService.getUserScore(userId)
                ));
        return ResponseEntity.ok(scores);
    }
} 