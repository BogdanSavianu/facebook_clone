package com.facebook.comment.controller;

import com.facebook.comment.payload.CommentDTO;
import com.facebook.comment.payload.UserDTO;
import com.facebook.comment.payload.request.CommentRequest;
import com.facebook.comment.payload.response.MessageResponse;
import com.facebook.comment.service.CommentService;
import com.facebook.comment.service.FileStorageService;
import com.facebook.comment.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentService commentService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/post/{postId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Long postId) {
        List<CommentDTO> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/post/{postId}/paged")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CommentDTO>> getPagedCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentDTO> comments = commentService.getCommentsByPostId(postId, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long id) {
        CommentDTO comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/{commentId}/replies")
    @PreAuthorize("hasRole('USER')") 
    public ResponseEntity<List<CommentDTO>> getCommentReplies(@PathVariable Long commentId) {
        List<CommentDTO> replies = commentService.getRepliesByParentId(commentId);
        return ResponseEntity.ok(replies);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentRequest commentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        UserDTO user = userService.getUserByUsername(username);
        Long userId = user.getId();
        
        CommentDTO createdComment = commentService.createComment(userId, commentRequest);
        return ResponseEntity.ok(createdComment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest commentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        UserDTO user = userService.getUserByUsername(username);
        Long userId = user.getId();
        
        CommentDTO updatedComment = commentService.updateComment(id, userId, commentRequest);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> deleteComment(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        UserDTO user = userService.getUserByUsername(username);
        Long userId = user.getId();
        
        commentService.deleteComment(id, userId);
        return ResponseEntity.ok(new MessageResponse("Comment deleted successfully"));
    }

    @GetMapping("/author/{authorId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CommentDTO>> getCommentsByAuthorId(@PathVariable Long authorId) {
        List<CommentDTO> comments = commentService.getCommentsByAuthorId(authorId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{id}/vote")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommentDTO> voteComment(
            @PathVariable Long id,
            @RequestParam boolean upvote) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        UserDTO user = userService.getUserByUsername(username);
        Long userId = user.getId();
        
        CommentDTO votedComment = commentService.voteComment(id, userId, upvote);
        return ResponseEntity.ok(votedComment);
    }
    
    @GetMapping("/public/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getPublicCommentsByPostId(@PathVariable Long postId) {
        List<CommentDTO> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }
    
    @GetMapping("/public/{id}")
    public ResponseEntity<CommentDTO> getPublicCommentById(@PathVariable Long id) {
        CommentDTO comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }
    
    @PutMapping("/internal/{id}/vote-count")
    public ResponseEntity<MessageResponse> updateInternalCommentVoteCount(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> payload) {
        Integer voteCount = payload.get("voteCount");
        if (voteCount == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("voteCount is required in payload"));
        }
        try {
            commentService.updateCommentVoteCount(id, voteCount);
            return ResponseEntity.ok(new MessageResponse("Comment vote count updated successfully."));
        } catch (RuntimeException e) {
            System.err.println("Error updating comment vote count for id " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error updating comment vote count: " + e.getMessage()));
        }
    }

    @GetMapping("/internal/post/{postId}/count")
    public ResponseEntity<Map<String, Long>> getInternalCommentCountByPostId(@PathVariable Long postId) {
        try {
            long count = commentService.countByPostIdAndParentIdIsNull(postId);
            return ResponseEntity.ok(Map.of("commentCount", count));
        } catch (Exception e) {
            logger.error("Error fetching comment count for post {}: {}", postId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("commentCount", 0L));
        }
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadCommentImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));
        }

        try {
            String fileUrl = fileStorageService.storeFile(file);
            return ResponseEntity.ok(Map.of("imageUrl", fileUrl)); 
        } catch (RuntimeException e) {
            logger.error("Could not upload comment image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("message", "Could not upload comment image: " + e.getMessage()));
        }
    }
} 