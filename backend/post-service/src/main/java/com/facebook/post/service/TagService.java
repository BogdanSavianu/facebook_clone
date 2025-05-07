package com.facebook.post.service;

import com.facebook.post.model.Tag;
import com.facebook.post.payload.TagDTO;

import java.util.List;
import java.util.Set;

public interface TagService {
    List<TagDTO> getAllTags();
    
    TagDTO getTagById(Long id);
    
    TagDTO createTag(String name);
    
    Set<Tag> getOrCreateTagsByNames(Set<String> tagNames);
} 