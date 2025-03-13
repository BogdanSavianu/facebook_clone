package com.utcn.contentservice.controller;

import com.utcn.contentservice.dto.ModeratorActionRequest;
import com.utcn.contentservice.entity.PostComment;
import com.utcn.contentservice.service.ModeratorService;
import com.utcn.contentservice.service.PostCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private PostCommentService commentService;
    
    @Autowired
    private ModeratorService moderatorService;

    @GetMapping
    public ResponseEntity<List<PostComment>> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostComment> getCommentById(@PathVariable Integer id) {
        PostComment comment = commentService.getCommentById(id);
        if (comment != null) {
            return ResponseEntity.ok(comment);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<PostComment>> getCommentsByPostId(@PathVariable Integer postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @PostMapping
    public ResponseEntity<PostComment> createComment(@RequestBody PostComment comment) {
        return new ResponseEntity<>(commentService.addComment(comment), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostComment> updateComment(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-ID", required = true) Integer userId,
            @RequestBody PostComment comment) {
        PostComment existingComment = commentService.getCommentById(id);
        if (existingComment == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!existingComment.getCommenterId().equals(userId) && !moderatorService.isModerator(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        comment.setId(id);
        return ResponseEntity.ok(commentService.updateComment(comment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-ID", required = true) Integer userId) {
        PostComment existingComment = commentService.getCommentById(id);
        if (existingComment == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!existingComment.getCommenterId().equals(userId) && !moderatorService.isModerator(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/moderate")
    public ResponseEntity<?> moderateComment(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-ID", required = true) Integer moderatorId,
            @RequestBody ModeratorActionRequest request) {
        
        if (!moderatorService.isModerator(moderatorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body("Only moderators can perform this action");
        }
        
        PostComment comment = commentService.getCommentById(id);
        if (comment == null) {
            return ResponseEntity.notFound().build();
        }
        
        if ("REMOVE".equals(request.getAction())) {
            commentService.deleteComment(id);
            return ResponseEntity.ok().body("Comment removed by moderator");
        } else if ("EDIT".equals(request.getAction())) {
            comment.setContent(request.getContent());
            return ResponseEntity.ok(commentService.updateComment(comment));
        } else {
            return ResponseEntity.badRequest().body("Invalid action");
        }
    }
} 