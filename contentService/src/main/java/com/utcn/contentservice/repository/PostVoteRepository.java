package com.utcn.contentservice.repository;

import com.utcn.contentservice.entity.Post;
import com.utcn.contentservice.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Integer> {
    /**
     * Find a vote by post and user
     */
    Optional<PostVote> findByPostAndUserId(Post post, Integer userId);
    
    /**
     * Find all votes by post
     */
    List<PostVote> findByPost(Post post);
    
    /**
     * Find all votes by user
     */
    List<PostVote> findByUserId(Integer userId);
    
    /**
     * Count total votes for a post
     */
    long countByPost(Post post);
    
    /**
     * Delete votes by post
     */
    void deleteByPost(Post post);
} 