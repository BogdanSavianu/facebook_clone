package com.facebook.post.controller;

import com.facebook.post.model.PostStatus;
import com.facebook.post.payload.PostDTO;
import com.facebook.post.payload.UserDTO;
import com.facebook.post.payload.request.PostRequest;
import com.facebook.post.payload.response.MessageResponse;
import com.facebook.post.service.PostService;
import com.facebook.post.service.UserService;
import com.facebook.post.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostService postService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<PostDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String titleSearch,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Boolean myPosts) {

        Long effectiveAuthorId = authorId;
        if (myPosts != null && myPosts) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserDTO currentUser = userService.getUserByUsername(username);
            effectiveAuthorId = currentUser.getId();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PostDTO> posts = postService.getAllPostsFiltered(pageable, tagName, titleSearch, effectiveAuthorId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        PostDTO post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody PostRequest postRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        UserDTO user = userService.getUserByUsername(username);
        Long userId = user.getId();
        
        PostDTO createdPost = postService.createPost(userId, postRequest);
        return ResponseEntity.ok(createdPost);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostDTO> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest postRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        UserDTO user = userService.getUserByUsername(username);
        Long userId = user.getId();
        
        PostDTO updatedPost = postService.updatePost(id, userId, postRequest);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> deletePost(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        UserDTO user = userService.getUserByUsername(username);
        Long userId = user.getId();
        
        postService.deletePost(id, userId);
        return ResponseEntity.ok(new MessageResponse("Post deleted successfully"));
    }

    @GetMapping("/author/{authorId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PostDTO>> getPostsByAuthor(@PathVariable Long authorId) {
        List<PostDTO> posts = postService.getPostsByAuthor(authorId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<PostDTO>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDTO> posts = postService.searchPostsByTitle(query, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/tag/{tagName}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<PostDTO>> getPostsByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDTO> posts = postService.getPostsByTag(tagName, pageable);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostDTO> updatePostStatus(
            @PathVariable Long id,
            @RequestParam PostStatus status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        UserDTO user = userService.getUserByUsername(username);
        Long userId = user.getId();
        
        PostDTO updatedPost = postService.updatePostStatus(id, userId, status);
        return ResponseEntity.ok(updatedPost);
    }

    @PostMapping("/{id}/vote")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostDTO> votePost(
            @PathVariable Long id,
            @RequestParam boolean upvote) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        UserDTO user = userService.getUserByUsername(username);
        Long userId = user.getId();
        
        PostDTO votedPost = postService.votePost(id, userId, upvote);
        return ResponseEntity.ok(votedPost);
    }
    
    @GetMapping("/public/{id}")
    public ResponseEntity<PostDTO> getPublicPostById(@PathVariable Long id) {
        PostDTO post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }
    
    @GetMapping("/public/author/{authorId}")
    public ResponseEntity<List<PostDTO>> getPublicPostsByAuthor(@PathVariable Long authorId) {
        List<PostDTO> posts = postService.getPostsByAuthor(authorId);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/public/tag/{tagName}")
    public ResponseEntity<Page<PostDTO>> getPublicPostsByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDTO> posts = postService.getPostsByTag(tagName, pageable);
        return ResponseEntity.ok(posts);
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadPostImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));
        }

        try {
            String fileUrl = fileStorageService.storeFile(file);
            return ResponseEntity.ok(Map.of("imageUrl", fileUrl)); 
        } catch (RuntimeException e) {
            logger.error("Could not upload image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("message", "Could not upload image: " + e.getMessage()));
        }
    }
    
    @PutMapping("/internal/{id}/vote-count")
    public ResponseEntity<MessageResponse> updateInternalPostVoteCount(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> payload) {
        Integer voteCount = payload.get("voteCount");
        if (voteCount == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("voteCount is required in payload"));
        }
        try {
            postService.updatePostVoteCount(id, voteCount);
            return ResponseEntity.ok(new MessageResponse("Post vote count updated successfully."));
        } catch (RuntimeException e) {
            logger.error("Error updating post vote count for id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error updating post vote count: " + e.getMessage()));
        }
    }
} 