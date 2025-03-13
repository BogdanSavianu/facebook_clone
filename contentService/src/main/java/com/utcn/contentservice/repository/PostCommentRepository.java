package com.utcn.contentservice.repository;

import com.utcn.contentservice.entity.PostComment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends CrudRepository<PostComment, Integer> {
    List<PostComment> findByPostId(Integer postId);
    List<PostComment> findByCommenterId(Integer commenterId);
} 