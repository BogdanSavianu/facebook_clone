package com.facebook.post.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class PostRequest {
    @NotBlank
    @Size(max = 255)
    private String title;
    
    @NotBlank
    private String content;
    
    private String imageUrl;
    
    private Set<String> tagNames;
    
    public PostRequest() {
    }
    
    public PostRequest(String title, String content, String imageUrl, Set<String> tagNames) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.tagNames = tagNames;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Set<String> getTagNames() {
        return tagNames;
    }
    
    public void setTagNames(Set<String> tagNames) {
        this.tagNames = tagNames;
    }
} 