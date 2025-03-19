package com.utcn.contentservice.service;

import com.utcn.contentservice.client.UserServiceClient;
import com.utcn.contentservice.entity.Postable;
import com.utcn.contentservice.entity.PostableVote;
import com.utcn.contentservice.repository.PostableRepository;
import com.utcn.contentservice.repository.PostableVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VoteService {

    @Autowired
    private PostableRepository postRepository;

    @Autowired
    private PostableVoteRepository postableVoteRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    private static final float POST_UPVOTE_SCORE = 5.0f;
    private static final float POST_DOWNVOTE_SCORE = -2.0f;
    private static final float COMMENT_UPVOTE_SCORE = 2.0f;
    private static final float COMMENT_DOWNVOTE_SCORE = -1.0f;

    public Optional<PostableVote> getVoteById(Integer id) {
        return postableVoteRepository.findById(id);
    }

    @Transactional
    public PostableVote createVote(PostableVote vote) {
        if (vote.getValue() != 1 && vote.getValue() != -1) {
            throw new IllegalArgumentException("Vote value must be either 1 (upvote) or -1 (downvote)");
        }
        
        Optional<PostableVote> existingVote = postableVoteRepository.findByPostableAndUserId(
            vote.getPostable(), vote.getUserId());
        
        if (existingVote.isPresent()) {
            throw new IllegalStateException("Vote already exists for this user and post");
        }

        PostableVote savedVote = postableVoteRepository.save(vote);
        notifyUserScoreChange(vote.getPostable().getUserId(),
                vote.getValue() == 1 ? POST_UPVOTE_SCORE : POST_DOWNVOTE_SCORE,
                vote.getValue() == 1 ? "post_upvote" : "post_downvote",
                vote.getPostable().getId());
        
        return savedVote;
    }

    @Transactional
    public PostableVote updateVote(PostableVote vote) {
        if (vote.getValue() != 1 && vote.getValue() != -1) {
            throw new IllegalArgumentException("Vote value must be either 1 (upvote) or -1 (downvote)");
        }

        Optional<PostableVote> existingVote = postableVoteRepository.findById(vote.getId());
        if (existingVote.isEmpty()) {
            throw new IllegalStateException("Vote not found");
        }

        PostableVote currentVote = existingVote.get();
        if (currentVote.getValue() != vote.getValue()) {
            // Notify score change for old value
            notifyUserScoreChange(currentVote.getPostable().getUserId(),
                    currentVote.getValue() == 1 ? -POST_UPVOTE_SCORE : -POST_DOWNVOTE_SCORE,
                    currentVote.getValue() == 1 ? "post_upvote_removed" : "post_downvote_removed",
                    currentVote.getPostable().getId());
            
            // Notify score change for new value
            notifyUserScoreChange(currentVote.getPostable().getUserId(),
                    vote.getValue() == 1 ? POST_UPVOTE_SCORE : POST_DOWNVOTE_SCORE,
                    vote.getValue() == 1 ? "post_upvote" : "post_downvote",
                    currentVote.getPostable().getId());
        }

        return postableVoteRepository.save(vote);
    }

    @Transactional
    public void deleteVote(Integer id) {
        Optional<PostableVote> vote = postableVoteRepository.findById(id);
        if (vote.isPresent()) {
            PostableVote currentVote = vote.get();
            notifyUserScoreChange(currentVote.getPostable().getUserId(),
                    currentVote.getValue() == 1 ? -POST_UPVOTE_SCORE : -POST_DOWNVOTE_SCORE,
                    currentVote.getValue() == 1 ? "post_upvote_removed" : "post_downvote_removed",
                    currentVote.getPostable().getId());
            postableVoteRepository.deleteById(id);
        }
    }

    @Transactional
    public int voteOnPost(Integer postId, Integer userId, Integer value) {
        if (value != 1 && value != -1) {
            throw new IllegalArgumentException("Vote value must be either 1 (upvote) or -1 (downvote)");
        }
        
        Postable post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));
        
        Optional<PostableVote> existingVote = postableVoteRepository.findByPostableAndUserId(post, userId);
        
        if (existingVote.isPresent()) {
            PostableVote vote = existingVote.get();
            // If the vote value is the same, remove the vote (toggling off)
            if (vote.getValue().equals(value)) {
                postableVoteRepository.delete(vote);
                notifyUserScoreChange(post.getUserId(),
                        vote.getValue() == 1 ? -POST_UPVOTE_SCORE : -POST_DOWNVOTE_SCORE,
                        vote.getValue() == 1 ? "post_upvote_removed" : "post_downvote_removed",
                        postId);
                return post.getVoteCount() - vote.getValue();
            } else {
                notifyUserScoreChange(post.getUserId(),
                        vote.getValue() == 1 ? -POST_UPVOTE_SCORE : -POST_DOWNVOTE_SCORE,
                        vote.getValue() == 1 ? "post_upvote_removed" : "post_downvote_removed",
                        postId);
                
                vote.setValue(value);
                postableVoteRepository.save(vote);
                
                notifyUserScoreChange(post.getUserId(),
                        value == 1 ? POST_UPVOTE_SCORE : POST_DOWNVOTE_SCORE,
                        value == 1 ? "post_upvote" : "post_downvote",
                        postId);
                
                return post.getVoteCount();
            }
        } else {
            PostableVote vote = new PostableVote();
            vote.setPostable(post);
            vote.setUserId(userId);
            vote.setValue(value);
            postableVoteRepository.save(vote);
            
            notifyUserScoreChange(post.getUserId(),
                    value == 1 ? POST_UPVOTE_SCORE : POST_DOWNVOTE_SCORE,
                    value == 1 ? "post_upvote" : "post_downvote",
                    postId);
            
            return post.getVoteCount();
        }
    }
    
    @Transactional
    public int voteOnComment(Integer commentId, Integer userId, Integer value) {
        if (value != 1 && value != -1) {
            throw new IllegalArgumentException("Vote value must be either 1 (upvote) or -1 (downvote)");
        }
        
        Postable comment = postableVoteRepository.findById(commentId)
                .map(PostableVote::getPostable)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));
        
        Optional<PostableVote> existingVote = postableVoteRepository.findByPostableAndUserId(comment, userId);
        
        if (existingVote.isPresent()) {
            PostableVote vote = existingVote.get();
            // If the vote value is the same, remove the vote (toggling off)
            if (vote.getValue().equals(value)) {
                postableVoteRepository.delete(vote);
                notifyUserScoreChange(userId, vote.getValue() * -1, 
                        vote.getValue() == 1 ? "comment_upvote_removed" : "comment_downvote_removed", 
                        commentId);
                return comment.getVoteCount() - vote.getValue();
            } else {
                notifyUserScoreChange(comment.getUserId(),
                        vote.getValue() == 1 ? -COMMENT_UPVOTE_SCORE : -COMMENT_DOWNVOTE_SCORE, 
                        vote.getValue() == 1 ? "comment_upvote_removed" : "comment_downvote_removed", 
                        commentId);
                
                vote.setValue(value);
                postableVoteRepository.save(vote);
                
                notifyUserScoreChange(comment.getUserId(),
                        value == 1 ? COMMENT_UPVOTE_SCORE : COMMENT_DOWNVOTE_SCORE, 
                        value == 1 ? "comment_upvote" : "comment_downvote", 
                        commentId);
                
                return comment.getVoteCount();
            }
        } else {
            PostableVote vote = new PostableVote();
            vote.setPostable(comment);
            vote.setUserId(userId);
            vote.setValue(value);
            postableVoteRepository.save(vote);
            
            notifyUserScoreChange(comment.getUserId(),
                    value == 1 ? COMMENT_UPVOTE_SCORE : COMMENT_DOWNVOTE_SCORE, 
                    value == 1 ? "comment_upvote" : "comment_downvote", 
                    commentId);
            
            return comment.getVoteCount();
        }
    }
    
    public List<PostableVote> getPostVotes(Integer postId) {
        Postable post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));
        return postableVoteRepository.findByPostable(post);
    }

    public List<PostableVote> getUserPostVotes(Integer userId) {
        return postableVoteRepository.findByUserId(userId);
    }
    
    private void notifyUserScoreChange(Integer userId, float adjustment, String reason, Integer referenceId) {
        try {
            userServiceClient.adjustUserScore(userId, adjustment, reason, referenceId);
        } catch (Exception e) {
            System.err.println("Failed to adjust user score: " + e.getMessage());
        }
    }
} 