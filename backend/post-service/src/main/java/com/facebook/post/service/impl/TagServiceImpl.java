package com.facebook.post.service.impl;

import com.facebook.post.model.Tag;
import com.facebook.post.payload.TagDTO;
import com.facebook.post.repository.TagRepository;
import com.facebook.post.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    @Override
    public List<TagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TagDTO getTagById(Long id) {
        return tagRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));
    }

    @Override
    @Transactional
    public TagDTO createTag(String name) {
        if (tagRepository.existsByName(name)) {
            return tagRepository.findByName(name)
                    .map(this::convertToDTO)
                    .orElseThrow(() -> new RuntimeException("Error finding tag after checking existence"));
        }
        
        Tag tag = new Tag();
        tag.setName(name);
        
        Tag savedTag = tagRepository.save(tag);
        return convertToDTO(savedTag);
    }

    @Override
    @Transactional
    public Set<Tag> getOrCreateTagsByNames(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }
        
        Set<Tag> tags = new HashSet<>();
        
        for (String name : tagNames) {
            Tag tag;
            if (tagRepository.existsByName(name)) {
                tag = tagRepository.findByName(name)
                        .orElseThrow(() -> new RuntimeException("Error finding tag after checking existence"));
            } else {
                tag = new Tag();
                tag.setName(name);
                tag = tagRepository.save(tag);
            }
            tags.add(tag);
        }
        
        return tags;
    }
    
    private TagDTO convertToDTO(Tag tag) {
        return new TagDTO(tag.getId(), tag.getName());
    }
} 