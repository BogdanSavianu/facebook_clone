package com.facebook.comment.service.impl;

import com.facebook.comment.model.Comment;
import com.facebook.comment.payload.CommentDTO;
import com.facebook.comment.payload.PostDTO;
import com.facebook.comment.payload.UserDTO;
import com.facebook.comment.payload.request.CommentRequest;
import com.facebook.comment.repository.CommentRepository;
import com.facebook.comment.service.CommentService;
import com.facebook.comment.service.PostService;
import com.facebook.comment.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PostService postService;

    @Override
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return commentRepository.findByPostIdAndParentIdIsNull(postId, sort).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CommentDTO> getCommentsByPostId(Long postId, Pageable pageable) {
        return commentRepository.findByPostIdAndParentIdIsNull(postId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public CommentDTO getCommentById(Long id) {
        return commentRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
    }

    @Override
    @Transactional
    public CommentDTO createComment(Long authorId, CommentRequest commentRequest) {
        PostDTO post = postService.getPostById(commentRequest.getPostId());
        
        if ("OUTDATED".equals(post.getStatus())) {
            throw new RuntimeException("Cannot comment on outdated post");
        }

        Comment parentComment = null;
        if (commentRequest.getParentId() != null) {
            parentComment = commentRepository.findById(commentRequest.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + commentRequest.getParentId()));
            
            if (!parentComment.getPostId().equals(commentRequest.getPostId())) {
                throw new RuntimeException("Parent comment belongs to a different post.");
            }
        }
        
        Comment comment = new Comment();
        comment.setPostId(commentRequest.getPostId());
        comment.setAuthorId(authorId);
        comment.setContent(commentRequest.getContent());
        comment.setImageUrl(commentRequest.getImageUrl());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setVoteCount(0);
        comment.setParent(parentComment);
        
        Comment savedComment = commentRepository.save(comment);
        
        if (parentComment == null && commentRepository.countByPostIdAndParentIdIsNull(commentRequest.getPostId()) == 1) {
             postService.updatePostStatus(commentRequest.getPostId(), "FIRST_REACTIONS");
        }
        
        return convertToDTO(savedComment);
    }

    @Override
    @Transactional
    public CommentDTO updateComment(Long id, Long userId, CommentRequest commentRequest) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        
        if (!comment.getAuthorId().equals(userId)) {
            throw new RuntimeException("User not authorized to update this comment");
        }
        
        comment.setContent(commentRequest.getContent());
        comment.setImageUrl(commentRequest.getImageUrl());
        
        Comment updatedComment = commentRepository.save(comment);
        return convertToDTO(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        
        if (!comment.getAuthorId().equals(userId)) {
            throw new RuntimeException("User not authorized to delete this comment");
        }
        
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDTO> getCommentsByAuthorId(Long authorId) {
        return commentRepository.findByAuthorIdOrderByCreatedAtDesc(authorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDTO voteComment(Long id, Long userId, boolean upvote) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        
        if (comment.getAuthorId().equals(userId)) {
            logger.warn("User {} attempted to vote on their own comment {}. This should ideally be caught by reaction-service.", userId, id);
            return convertToDTO(comment);
        }

        logger.warn("voteComment in CommentServiceImpl was called. This might be redundant if reaction-service handles all votes.");
        return convertToDTO(comment);
    }

    @Override
    @Transactional
    public void updateCommentVoteCount(Long commentId, int voteCount) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        comment.setVoteCount(voteCount);
        commentRepository.save(comment);
        logger.info("Updated vote count for comment {} to {}", commentId, voteCount);
    }

    @Override
    public List<CommentDTO> getRepliesByParentId(Long parentId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt"); 
        return commentRepository.findByParentId(parentId, sort).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countByPostIdAndParentIdIsNull(Long postId) {
        return commentRepository.countByPostIdAndParentIdIsNull(postId);
    }
    
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setPostId(comment.getPostId());
        commentDTO.setAuthorId(comment.getAuthorId());
        
        try {
            UserDTO author = userService.getUserById(comment.getAuthorId());
            commentDTO.setAuthorUsername(author.getUsername());
            commentDTO.setAuthorScore(author.getUserScore());
        } catch (Exception e) {
            commentDTO.setAuthorUsername("Unknown");
            commentDTO.setAuthorScore(0.0);
        }
        
        commentDTO.setContent(comment.getContent());
        commentDTO.setCreatedAt(comment.getCreatedAt());
        commentDTO.setImageUrl(comment.getImageUrl());
        commentDTO.setVoteCount(comment.getVoteCount());
        commentDTO.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);

        long replyCount = commentRepository.countByParentId(comment.getId());
        commentDTO.setReplyCount((int) replyCount);

        return commentDTO;
    }
} 