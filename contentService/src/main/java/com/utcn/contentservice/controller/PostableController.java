package com.utcn.contentservice.controller;

import com.utcn.contentservice.dto.ModeratorActionRequest;
import com.utcn.contentservice.entity.Postable;
import com.utcn.contentservice.service.ModeratorService;
import com.utcn.contentservice.service.PostableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
public class PostableController {

    private final PostableService postableService;
    private final ModeratorService moderatorService;

    @Autowired
    public PostableController(PostableService postableService, ModeratorService moderatorService) {
        this.postableService = postableService;
        this.moderatorService = moderatorService;
    }

    @GetMapping
    public ResponseEntity<List<Postable>> getAllPosts() {
        return ResponseEntity.ok(postableService.getAllPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Postable> getPostById(@PathVariable Integer id) {
        Optional<Postable> post = postableService.getPostById(id);
        if (post.isPresent()) {
            return ResponseEntity.ok(post.get());
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/comments")
    public ResponseEntity<List<Postable>> getAllComments() {
        return ResponseEntity.ok(postableService.getAllComments());
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<Postable> getCommentById(@PathVariable Integer id) {
        Optional<Postable> comment = postableService.getCommentById(id);
        if (comment.isPresent()) {
            return ResponseEntity.ok(comment.get());
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/post/{postId}/comments")
    public ResponseEntity<List<Postable>> getCommentsByPostId(@PathVariable Integer postId) {
        return ResponseEntity.ok(postableService.getCommentsByPostId(postId));
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, "application/json;charset=UTF-8"})
    public ResponseEntity<Postable> createPost(@RequestBody Postable post) {
        Postable createdPost = postableService.addPost(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @PostMapping(value = "/{postId}/comments", consumes = {MediaType.APPLICATION_JSON_VALUE, "application/json;charset=UTF-8"})
    public ResponseEntity<Postable> addComment(@PathVariable Integer postId, @RequestBody Postable comment) {
        Optional<Postable> post = postableService.getPostById(postId);
        if (post.isPresent()) {
            comment.setParent(post.get());
            Postable createdComment = postableService.addPost(comment);
            return ResponseEntity.ok(createdComment);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, "application/json;charset=UTF-8"})
    public ResponseEntity<Postable> updatePost(
            @PathVariable Integer id,
            @RequestBody Postable post,
            @RequestHeader("X-User-ID") Integer userId) {
        Optional<Postable> existingPost = postableService.getPostById(id);
        if (existingPost.isPresent()) {
            if (!existingPost.get().getUserId().equals(userId)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            post.setId(id);
            Postable updatedPost = postableService.updatePost(post);
            return ResponseEntity.ok(updatedPost);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Integer id,
            @RequestHeader("X-User-ID") Integer userId) {
        Optional<Postable> existingPost = postableService.getPostById(id);
        if (existingPost.isPresent()) {
            if (!existingPost.get().getUserId().equals(userId)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            postableService.deletePost(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{id}/moderate")
    public ResponseEntity<Postable> moderatePost(
            @PathVariable Integer id,
            @RequestHeader("X-User-ID") Integer userId,
            @RequestBody ModeratorActionRequest request) {
        if (!moderatorService.isModerator(userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Postable> existingPost = postableService.getPostById(id);
        if (existingPost.isPresent()) {
            Postable post = existingPost.get();
            switch (request.getAction()) {
                case "APPROVE":
                    post.setStatus("APPROVED");
                    break;
                case "REJECT":
                    post.setStatus("REJECTED");
                    post.setBody(request.getContent());
                    break;
                default:
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Postable updatedPost = postableService.updatePost(post);
            return ResponseEntity.ok(updatedPost);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
} 