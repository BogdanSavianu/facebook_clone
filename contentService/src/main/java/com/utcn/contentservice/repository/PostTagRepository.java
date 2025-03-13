package com.utcn.contentservice.repository;

import com.utcn.contentservice.entity.PostTag;
import com.utcn.contentservice.entity.PostTag.PostTagId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends CrudRepository<PostTag, PostTagId> {
    List<PostTag> findByIdPostId(Integer postId);
    List<PostTag> findByIdTaggedPersonId(Integer taggedPersonId);
} 