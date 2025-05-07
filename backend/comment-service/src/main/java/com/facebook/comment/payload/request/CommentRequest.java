package com.facebook.comment.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommentRequest {
    @NotNull
    private Long postId;
    
    @NotBlank
    private String content;
    
    private String imageUrl;
    
    private Long parentId;
    
    public CommentRequest() {
    }
    
    public CommentRequest(Long postId, String content, String imageUrl, Long parentId) {
        this.postId = postId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.parentId = parentId;
    }
    
    public Long getPostId() {
        return postId;
    }
    
    public void setPostId(Long postId) {
        this.postId = postId;
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
} 