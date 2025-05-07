package com.facebook.post.payload;

import com.facebook.post.model.PostStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class PostDTO {
    private Long id;
    private Long authorId;
    private String authorUsername;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String imageUrl;
    private PostStatus status;
    private Integer voteCount;
    private Set<TagDTO> tags;
    private Integer commentCount;
    
    public PostDTO() {
    }
    
    public PostDTO(Long id, Long authorId, String authorUsername, String title, String content, 
                  LocalDateTime createdAt, String imageUrl, PostStatus status, Integer voteCount, Set<TagDTO> tags) {
        this.id = id;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
        this.status = status;
        this.voteCount = voteCount;
        this.tags = tags;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
    
    public String getAuthorUsername() {
        return authorUsername;
    }
    
    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public PostStatus getStatus() {
        return status;
    }
    
    public void setStatus(PostStatus status) {
        this.status = status;
    }
    
    public Integer getVoteCount() {
        return voteCount;
    }
    
    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }
    
    public Set<TagDTO> getTags() {
        return tags;
    }
    
    public void setTags(Set<TagDTO> tags) {
        this.tags = tags;
    }
    
    public Integer getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }
} 