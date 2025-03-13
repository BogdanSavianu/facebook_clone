package com.utcn.socialgraphservice.repository;

import com.utcn.socialgraphservice.entity.Friendship;
import com.utcn.socialgraphservice.entity.Friendship.FriendshipId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends CrudRepository<Friendship, FriendshipId> {
    List<Friendship> findByRequesterId(Integer requesterId);
    List<Friendship> findByAddresseeId(Integer addresseeId);
    List<Friendship> findByRequesterIdAndStatus(Integer requesterId, String status);
    List<Friendship> findByAddresseeIdAndStatus(Integer addresseeId, String status);
} 