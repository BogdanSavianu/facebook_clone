package com.facebook.comment.service;

import com.facebook.comment.payload.CommentDTO;
import com.facebook.comment.payload.request.CommentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    List<CommentDTO> getCommentsByPostId(Long postId);
    
    Page<CommentDTO> getCommentsByPostId(Long postId, Pageable pageable);
    
    CommentDTO getCommentById(Long id);
    
    CommentDTO createComment(Long authorId, CommentRequest commentRequest);
    
    CommentDTO updateComment(Long id, Long userId, CommentRequest commentRequest);
    
    void deleteComment(Long id, Long userId);
    
    List<CommentDTO> getCommentsByAuthorId(Long authorId);
    
    CommentDTO voteComment(Long id, Long userId, boolean upvote);
    
    void updateCommentVoteCount(Long commentId, int voteCount);

    List<CommentDTO> getRepliesByParentId(Long parentId);

    long countByPostIdAndParentIdIsNull(Long postId);
} 