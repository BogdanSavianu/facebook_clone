package com.utcn.contentservice.controller;

import com.utcn.contentservice.entity.PostableVote;
import com.utcn.contentservice.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/votes")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @GetMapping("/{id}")
    public ResponseEntity<PostableVote> getVoteById(@PathVariable Integer id) {
        Optional<PostableVote> vote = voteService.getVoteById(id);
        return vote.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PostableVote> createVote(@RequestBody PostableVote vote) {
        PostableVote createdVote = voteService.createVote(vote);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVote);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostableVote> updateVote(
            @PathVariable Integer id,
            @RequestBody PostableVote vote,
            @RequestHeader("X-User-ID") Integer userId) {
        Optional<PostableVote> existingVote = voteService.getVoteById(id);
        if (existingVote.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!existingVote.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        vote.setId(id);
        PostableVote updatedVote = voteService.updateVote(vote);
        return ResponseEntity.ok(updatedVote);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVote(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        Optional<PostableVote> existingVote = voteService.getVoteById(id);
        if (existingVote.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!existingVote.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        voteService.deleteVote(id);
        return ResponseEntity.noContent().build();
    }

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
    public ResponseEntity<List<PostableVote>> getPostVotes(@PathVariable Integer postId) {
        return ResponseEntity.ok(voteService.getPostVotes(postId));
    }

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<List<PostableVote>> getUserPostVotes(@PathVariable Integer userId) {
        return ResponseEntity.ok(voteService.getUserPostVotes(userId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
} 