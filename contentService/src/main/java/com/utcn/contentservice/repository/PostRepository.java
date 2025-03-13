package com.utcn.contentservice.repository;

import com.utcn.contentservice.entity.Post;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Integer> {
    List<Post> findByUserId(Integer userId);
    List<Post> findByGroupId(Integer groupId);
} 