package com.utcn.contentservice.service;

import com.utcn.contentservice.entity.PostTag;
import com.utcn.contentservice.entity.PostTag.PostTagId;
import com.utcn.contentservice.repository.PostTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostTagService {

    @Autowired
    private PostTagRepository postTagRepository;

    public List<PostTag> retrieveTags() {
        List<PostTag> tags = new ArrayList<>();
        postTagRepository.findAll().forEach(tags::add);
        return tags;
    }

    public PostTag retrieveTagById(PostTagId id) {
        Optional<PostTag> tag = postTagRepository.findById(id);
        return tag.orElse(null);
    }

    public List<PostTag> retrieveTagsByPostId(Integer postId) {
        return postTagRepository.findByIdPostId(postId);
    }

    public List<PostTag> retrieveTagsByPersonId(Integer taggedPersonId) {
        return postTagRepository.findByIdTaggedPersonId(taggedPersonId);
    }

    public PostTag addTag(PostTag tag) {
        return postTagRepository.save(tag);
    }

    public PostTag updateTag(PostTag tag) {
        return postTagRepository.save(tag);
    }

    public void deleteTag(PostTagId id) {
        postTagRepository.deleteById(id);
    }
} 