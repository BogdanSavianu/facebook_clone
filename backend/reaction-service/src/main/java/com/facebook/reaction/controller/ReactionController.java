package com.facebook.reaction.controller;

import com.facebook.reaction.model.EntityType;
import com.facebook.reaction.model.ReactionType;
import com.facebook.reaction.payload.request.ReactionRequest;
import com.facebook.reaction.payload.response.MessageResponse;
import com.facebook.reaction.payload.response.ReactionResponse;
import com.facebook.reaction.service.ReactionService;
import com.facebook.reaction.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reactions")
public class ReactionController {
    
    @Autowired
    private ReactionService reactionService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addReaction(@Valid @RequestBody ReactionRequest reactionRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Long userId = userService.getUserIdByUsername(username);
            
            if (userId == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
            }
            
            EntityType entityType = EntityType.valueOf(reactionRequest.getEntityType());
            ReactionType reactionType = reactionRequest.getIsLike() ? ReactionType.LIKE : ReactionType.DISLIKE;
            
            ReactionResponse response = reactionService.reactToEntity(
                    userId,
                    entityType,
                    reactionRequest.getEntityId(),
                    reactionType
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid entity type: " + reactionRequest.getEntityType()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error processing reaction: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{entityType}/{entityId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeReaction(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Long userId = userService.getUserIdByUsername(username);
            
            if (userId == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
            }
            
            EntityType entityTypeEnum = EntityType.valueOf(entityType.toUpperCase());
            
            ReactionResponse response = reactionService.removeReaction(
                    userId,
                    entityTypeEnum,
                    entityId
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid entity type: " + entityType));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error removing reaction: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<?> getReactionStats(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        try {
            Long userId = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
                String username = authentication.getName();
                userId = userService.getUserIdByUsername(username);
            }
            
            EntityType entityTypeEnum = EntityType.valueOf(entityType.toUpperCase());
            
            ReactionResponse response = reactionService.getReactionStats(
                    entityTypeEnum,
                    entityId,
                    userId,
                    null,
                    null
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid entity type: " + entityType));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error getting reaction stats: " + e.getMessage()));
        }
    }
} 