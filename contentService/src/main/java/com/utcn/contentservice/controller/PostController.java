package com.utcn.contentservice.controller;

import com.utcn.contentservice.dto.ModeratorActionRequest;
import com.utcn.contentservice.entity.Post;
import com.utcn.contentservice.service.ModeratorService;
import com.utcn.contentservice.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;
    
    @Autowired
    private ModeratorService moderatorService;

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.retrievePosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Integer id) {
        Post post = postService.retrievePostById(id);
        if (post != null) {
            return ResponseEntity.ok(post);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(postService.retrievePostsByUserId(userId));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Post>> getPostsByGroupId(@PathVariable Integer groupId) {
        return ResponseEntity.ok(postService.retrievePostsByGroupId(groupId));
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        return new ResponseEntity<>(postService.addPost(post), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-ID", required = true) Integer userId,
            @RequestBody Post post) {
        Post existingPost = postService.retrievePostById(id);
        if (existingPost == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!existingPost.getUserId().equals(userId) && !moderatorService.isModerator(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        post.setId(id);
        return ResponseEntity.ok(postService.updatePost(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-ID", required = true) Integer userId) {
        Post existingPost = postService.retrievePostById(id);
        if (existingPost == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!existingPost.getUserId().equals(userId) && !moderatorService.isModerator(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        postService.deletePostById(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/moderate")
    public ResponseEntity<?> moderatePost(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-ID", required = true) Integer moderatorId,
            @RequestBody ModeratorActionRequest request) {
        
        if (!moderatorService.isModerator(moderatorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body("Only moderators can perform this action");
        }
        
        Post post = postService.retrievePostById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        
        if ("REMOVE".equals(request.getAction())) {
            postService.deletePostById(id);
            return ResponseEntity.ok().body("Post removed by moderator");
        } else if ("EDIT".equals(request.getAction())) {
            post.setBody(request.getContent());
            return ResponseEntity.ok(postService.updatePost(post));
        } else {
            return ResponseEntity.badRequest().body("Invalid action");
        }
    }
} 