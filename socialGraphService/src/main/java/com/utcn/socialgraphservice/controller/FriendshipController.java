package com.utcn.socialgraphservice.controller;

import com.utcn.socialgraphservice.entity.Friendship;
import com.utcn.socialgraphservice.entity.Friendship.FriendshipId;
import com.utcn.socialgraphservice.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friendships")
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    @GetMapping
    public ResponseEntity<List<Friendship>> getAllFriendships() {
        return ResponseEntity.ok(friendshipService.getAllFriendships());
    }

    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<Friendship>> getFriendshipsByRequesterId(@PathVariable Integer requesterId) {
        return ResponseEntity.ok(friendshipService.getFriendshipsByRequesterId(requesterId));
    }

    @GetMapping("/addressee/{addresseeId}")
    public ResponseEntity<List<Friendship>> getFriendshipsByAddresseeId(@PathVariable Integer addresseeId) {
        return ResponseEntity.ok(friendshipService.getFriendshipsByAddresseeId(addresseeId));
    }

    @GetMapping("/pending/{addresseeId}")
    public ResponseEntity<List<Friendship>> getPendingFriendRequests(@PathVariable Integer addresseeId) {
        return ResponseEntity.ok(friendshipService.getPendingFriendRequests(addresseeId));
    }

    @GetMapping("/accepted/{userId}")
    public ResponseEntity<List<Friendship>> getAcceptedFriendships(@PathVariable Integer userId) {
        return ResponseEntity.ok(friendshipService.getAcceptedFriendships(userId));
    }

    @PostMapping
    public ResponseEntity<Friendship> sendFriendRequest(@RequestBody Friendship friendship) {
        return new ResponseEntity<>(friendshipService.sendFriendRequest(friendship), HttpStatus.CREATED);
    }

    @PutMapping("/{requesterId}/{addresseeId}/status")
    public ResponseEntity<Friendship> updateFriendshipStatus(
            @PathVariable Integer requesterId,
            @PathVariable Integer addresseeId,
            @RequestParam String status) {
        FriendshipId id = new FriendshipId(requesterId, addresseeId);
        Friendship updatedFriendship = friendshipService.updateFriendshipStatus(id, status);
        if (updatedFriendship != null) {
            return ResponseEntity.ok(updatedFriendship);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{requesterId}/{addresseeId}")
    public ResponseEntity<Void> deleteFriendship(
            @PathVariable Integer requesterId,
            @PathVariable Integer addresseeId) {
        FriendshipId id = new FriendshipId(requesterId, addresseeId);
        Friendship existingFriendship = friendshipService.getFriendshipById(id);
        if (existingFriendship == null) {
            return ResponseEntity.notFound().build();
        }
        friendshipService.deleteFriendship(id);
        return ResponseEntity.noContent().build();
    }
} 