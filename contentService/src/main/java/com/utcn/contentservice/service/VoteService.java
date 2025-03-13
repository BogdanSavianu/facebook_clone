package com.utcn.contentservice.service;

import com.utcn.contentservice.client.UserServiceClient;
import com.utcn.contentservice.entity.CommentVote;
import com.utcn.contentservice.entity.Post;
import com.utcn.contentservice.entity.PostComment;
import com.utcn.contentservice.entity.PostVote;
import com.utcn.contentservice.repository.CommentVoteRepository;
import com.utcn.contentservice.repository.PostRepository;
import com.utcn.contentservice.repository.PostVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling content voting operations
 */
@Service
public class VoteService {

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private PostVoteRepository postVoteRepository;
    
    @Autowired
    private CommentVoteRepository commentVoteRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    // Score adjustment constants
    private static final float POST_UPVOTE_SCORE = 5.0f;
    private static final float POST_DOWNVOTE_SCORE = -2.0f;
    private static final float COMMENT_UPVOTE_SCORE = 2.0f;
    private static final float COMMENT_DOWNVOTE_SCORE = -1.0f;

    /**
     * Vote on a post
     * 
     * @param postId The ID of the post to vote on
     * @param userId The ID of the user voting
     * @param value The vote value: 1 for upvote, -1 for downvote
     * @return The updated vote count for the post
     */
    @Transactional
    public int voteOnPost(Integer postId, Integer userId, Integer value) {
        if (value != 1 && value != -1) {
            throw new IllegalArgumentException("Vote value must be either 1 (upvote) or -1 (downvote)");
        }
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));
        
        // Check if the user has already voted on this post
        Optional<PostVote> existingVote = postVoteRepository.findByPostAndUserId(post, userId);
        
        if (existingVote.isPresent()) {
            PostVote vote = existingVote.get();
            // If the vote value is the same, remove the vote (toggling off)
            if (vote.getValue().equals(value)) {
                postVoteRepository.delete(vote);
                notifyUserScoreChange(userId, vote.getValue() * -1, 
                        vote.getValue() == 1 ? "post_upvote_removed" : "post_downvote_removed", 
                        postId);
                return post.getVoteCount() - vote.getValue();
            } else {
                // Change the vote direction
                // First, reverse the previous score adjustment
                notifyUserScoreChange(post.getUserId(), 
                        vote.getValue() == 1 ? -POST_UPVOTE_SCORE : -POST_DOWNVOTE_SCORE, 
                        vote.getValue() == 1 ? "post_upvote_removed" : "post_downvote_removed", 
                        postId);
                
                // Update the vote
                vote.setValue(value);
                postVoteRepository.save(vote);
                
                // Apply new score adjustment
                notifyUserScoreChange(post.getUserId(), 
                        value == 1 ? POST_UPVOTE_SCORE : POST_DOWNVOTE_SCORE, 
                        value == 1 ? "post_upvote" : "post_downvote", 
                        postId);
                
                return post.getVoteCount();
            }
        } else {
            // Create a new vote
            PostVote vote = new PostVote();
            vote.setPost(post);
            vote.setUserId(userId);
            vote.setValue(value);
            postVoteRepository.save(vote);
            
            // Adjust the post author's score
            notifyUserScoreChange(post.getUserId(), 
                    value == 1 ? POST_UPVOTE_SCORE : POST_DOWNVOTE_SCORE, 
                    value == 1 ? "post_upvote" : "post_downvote", 
                    postId);
            
            return post.getVoteCount();
        }
    }
    
    /**
     * Vote on a comment
     * 
     * @param commentId The ID of the comment to vote on
     * @param userId The ID of the user voting
     * @param value The vote value: 1 for upvote, -1 for downvote
     * @return The updated vote count for the comment
     */
    @Transactional
    public int voteOnComment(Integer commentId, Integer userId, Integer value) {
        if (value != 1 && value != -1) {
            throw new IllegalArgumentException("Vote value must be either 1 (upvote) or -1 (downvote)");
        }
        
        PostComment comment = commentVoteRepository.findById(commentId)
                .map(vote -> vote.getComment())
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));
        
        // Check if the user has already voted on this comment
        Optional<CommentVote> existingVote = commentVoteRepository.findByCommentAndUserId(comment, userId);
        
        if (existingVote.isPresent()) {
            CommentVote vote = existingVote.get();
            // If the vote value is the same, remove the vote (toggling off)
            if (vote.getValue().equals(value)) {
                commentVoteRepository.delete(vote);
                notifyUserScoreChange(userId, vote.getValue() * -1, 
                        vote.getValue() == 1 ? "comment_upvote_removed" : "comment_downvote_removed", 
                        commentId);
                return comment.getVoteCount() - vote.getValue();
            } else {
                // Change the vote direction
                // First, reverse the previous score adjustment
                notifyUserScoreChange(comment.getCommenterId(), 
                        vote.getValue() == 1 ? -COMMENT_UPVOTE_SCORE : -COMMENT_DOWNVOTE_SCORE, 
                        vote.getValue() == 1 ? "comment_upvote_removed" : "comment_downvote_removed", 
                        commentId);
                
                // Update the vote
                vote.setValue(value);
                commentVoteRepository.save(vote);
                
                // Apply new score adjustment
                notifyUserScoreChange(comment.getCommenterId(), 
                        value == 1 ? COMMENT_UPVOTE_SCORE : COMMENT_DOWNVOTE_SCORE, 
                        value == 1 ? "comment_upvote" : "comment_downvote", 
                        commentId);
                
                return comment.getVoteCount();
            }
        } else {
            // Create a new vote
            CommentVote vote = new CommentVote();
            vote.setComment(comment);
            vote.setUserId(userId);
            vote.setValue(value);
            commentVoteRepository.save(vote);
            
            // Adjust the comment author's score
            notifyUserScoreChange(comment.getCommenterId(), 
                    value == 1 ? COMMENT_UPVOTE_SCORE : COMMENT_DOWNVOTE_SCORE, 
                    value == 1 ? "comment_upvote" : "comment_downvote", 
                    commentId);
            
            return comment.getVoteCount();
        }
    }
    
    /**
     * Get all votes for a post
     */
    public List<PostVote> getPostVotes(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));
        return postVoteRepository.findByPost(post);
    }
    
    /**
     * Get all votes by a user
     */
    public List<PostVote> getUserPostVotes(Integer userId) {
        return postVoteRepository.findByUserId(userId);
    }
    
    /**
     * Notify the user service about a score change.
     */
    private void notifyUserScoreChange(Integer userId, float adjustment, String reason, Integer referenceId) {
        try {
            userServiceClient.adjustUserScore(userId, adjustment, reason, referenceId);
        } catch (Exception e) {
            // Log the error but don't fail the vote operation
            System.err.println("Failed to adjust user score: " + e.getMessage());
        }
    }
} 