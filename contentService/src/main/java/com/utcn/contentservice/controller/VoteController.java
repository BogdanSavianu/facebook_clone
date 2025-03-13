package com.utcn.contentservice.controller;

import com.utcn.contentservice.entity.PostVote;
import com.utcn.contentservice.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/votes")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @PostMapping("/posts/{postId}")
    public ResponseEntity<Map<String, Integer>> voteOnPost(
            @PathVariable Integer postId,
            @RequestParam Integer userId,
            @RequestParam Integer value) {
        int voteCount = voteService.voteOnPost(postId, userId, value);
        return ResponseEntity.ok(Map.of("voteCount", voteCount));
    }

    @PostMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, Integer>> voteOnComment(
            @PathVariable Integer commentId,
            @RequestParam Integer userId,
            @RequestParam Integer value) {
        int voteCount = voteService.voteOnComment(commentId, userId, value);
        return ResponseEntity.ok(Map.of("voteCount", voteCount));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<List<PostVote>> getPostVotes(@PathVariable Integer postId) {
        return ResponseEntity.ok(voteService.getPostVotes(postId));
    }

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<List<PostVote>> getUserPostVotes(@PathVariable Integer userId) {
        return ResponseEntity.ok(voteService.getUserPostVotes(userId));
    }
} 