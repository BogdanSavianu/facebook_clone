package com.facebook.comment.repository;

import com.facebook.comment.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndParentIdIsNull(Long postId, Sort sort);
    
    Page<Comment> findByPostIdAndParentIdIsNull(Long postId, Pageable pageable);
    
    List<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    List<Comment> findByParentId(Long parentId, Sort sort);

    long countByPostIdAndParentIdIsNull(Long postId);

    long countByParentId(Long parentId);
} 