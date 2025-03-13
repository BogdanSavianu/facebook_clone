package com.utcn.contentservice.repository;

import com.utcn.contentservice.entity.CommentVote;
import com.utcn.contentservice.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, Integer> {
    /**
     * Find a vote by comment and user
     */
    Optional<CommentVote> findByCommentAndUserId(PostComment comment, Integer userId);
    
    /**
     * Find all votes by comment
     */
    List<CommentVote> findByComment(PostComment comment);
    
    /**
     * Find all votes by user
     */
    List<CommentVote> findByUserId(Integer userId);
    
    /**
     * Count total votes for a comment
     */
    long countByComment(PostComment comment);
    
    /**
     * Delete votes by comment
     */
    void deleteByComment(PostComment comment);
} 