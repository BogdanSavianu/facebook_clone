package com.utcn.userservice.controller;

import com.utcn.userservice.dto.BanUserRequest;
import com.utcn.userservice.entity.User;
import com.utcn.userservice.service.ModeratorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/moderator")
public class ModeratorController {

    @Autowired
    private ModeratorService moderatorService;

    @PostMapping("/ban")
    public ResponseEntity<User> banUser(
            @RequestHeader("X-User-ID") Integer moderatorId,
            @Valid @RequestBody BanUserRequest request) {
        try {
            User bannedUser = moderatorService.banUser(moderatorId, request.getUserId(), request.getReason());
            return ResponseEntity.ok(bannedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/unban/{userId}")
    public ResponseEntity<User> unbanUser(
            @RequestHeader("X-User-ID") Integer moderatorId,
            @PathVariable Integer userId) {
        try {
            User unbannedUser = moderatorService.unbanUser(moderatorId, userId);
            return ResponseEntity.ok(unbannedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/promote/{userId}")
    public ResponseEntity<User> promoteToModerator(
            @RequestHeader("X-User-ID") Integer adminId,
            @PathVariable Integer userId) {
        try {
            User promotedUser = moderatorService.promoteToModerator(adminId, userId);
            return ResponseEntity.ok(promotedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/demote/{userId}")
    public ResponseEntity<User> demoteFromModerator(
            @RequestHeader("X-User-ID") Integer adminId,
            @PathVariable Integer userId) {
        try {
            User demotedUser = moderatorService.demoteFromModerator(adminId, userId);
            return ResponseEntity.ok(demotedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/isModerator/{userId}")
    public ResponseEntity<Boolean> isModerator(@PathVariable Integer userId) {
        boolean isModerator = moderatorService.isModerator(userId);
        return ResponseEntity.ok(isModerator);
    }

    @GetMapping("/isBanned/{userId}")
    public ResponseEntity<Boolean> isBanned(@PathVariable Integer userId) {
        boolean isBanned = moderatorService.isBanned(userId);
        return ResponseEntity.ok(isBanned);
    }
} 