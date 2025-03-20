package com.utcn.socialgraphservice.service;

import com.utcn.socialgraphservice.entity.Friendship;
import com.utcn.socialgraphservice.entity.Friendship.FriendshipId;
import com.utcn.socialgraphservice.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    public List<Friendship> getAllFriendships() {
        List<Friendship> friendships = new ArrayList<>();
        friendshipRepository.findAll().forEach(friendships::add);
        return friendships;
    }

    public Friendship getFriendshipById(FriendshipId id) {
        Optional<Friendship> friendship = friendshipRepository.findById(id);
        return friendship.orElse(null);
    }

    public List<Friendship> getFriendshipsByRequesterId(Integer requesterId) {
        return friendshipRepository.findByRequesterId(requesterId);
    }

    public List<Friendship> getFriendshipsByAddresseeId(Integer addresseeId) {
        return friendshipRepository.findByAddresseeId(addresseeId);
    }

    public List<Friendship> getPendingFriendRequests(Integer addresseeId) {
        return friendshipRepository.findByAddresseeIdAndStatus(addresseeId, "PENDING");
    }

    public List<Friendship> getAcceptedFriendships(Integer userId) {
        List<Friendship> allFriendships = new ArrayList<>();
        allFriendships.addAll(friendshipRepository.findByRequesterIdAndStatus(userId, "ACCEPTED"));
        allFriendships.addAll(friendshipRepository.findByAddresseeIdAndStatus(userId, "ACCEPTED"));
        return allFriendships;
    }

    public Friendship sendFriendRequest(Friendship friendship) {
        friendship.setStatus("PENDING");
        friendship.setStartedAt(LocalDate.now());
        return friendshipRepository.save(friendship);
    }

    public Friendship updateFriendshipStatus(FriendshipId id, String status) {
        Optional<Friendship> existingFriendship = friendshipRepository.findById(id);
        if (existingFriendship.isPresent()) {
            Friendship friendship = existingFriendship.get();
            friendship.setStatus(status);
            if (status.equals("ACCEPTED")) {
                friendship.setStartedAt(LocalDate.now());
            }
            return friendshipRepository.save(friendship);
        }
        return null;
    }

    public void deleteFriendship(FriendshipId id) {
        friendshipRepository.deleteById(id);
    }
} 