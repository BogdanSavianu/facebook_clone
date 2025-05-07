package com.facebook.post.controller;

import com.facebook.post.payload.TagDTO;
import com.facebook.post.service.TagService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagDTO> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Long id) {
        TagDTO tag = tagService.getTagById(id);
        return ResponseEntity.ok(tag);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TagDTO> createTag(@RequestParam @NotBlank String name) {
        TagDTO newTag = tagService.createTag(name);
        return ResponseEntity.ok(newTag);
    }
    
    @GetMapping("/public")
    public ResponseEntity<List<TagDTO>> getPublicTags() {
        List<TagDTO> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }
    
    @GetMapping("/public/{id}")
    public ResponseEntity<TagDTO> getPublicTagById(@PathVariable Long id) {
        TagDTO tag = tagService.getTagById(id);
        return ResponseEntity.ok(tag);
    }
} 