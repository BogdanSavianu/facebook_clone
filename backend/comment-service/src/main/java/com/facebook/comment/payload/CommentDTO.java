package com.facebook.comment.payload;

import java.time.LocalDateTime;

public class CommentDTO {
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorUsername;
    private Double authorScore;
    private String content;
    private LocalDateTime createdAt;
    private String imageUrl;
    private Integer voteCount;
    private Long parentId;
    private Integer replyCount;
    
    public CommentDTO() {
    }
    
    public CommentDTO(Long id, Long postId, Long parentId, Long authorId, String authorUsername, Double authorScore, 
                     String content, LocalDateTime createdAt, String imageUrl, Integer voteCount) {
        this.id = id;
        this.postId = postId;
        this.parentId = parentId;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.authorScore = authorScore;
        this.content = content;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
        this.voteCount = voteCount;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getPostId() {
        return postId;
    }
    
    public void setPostId(Long postId) {
        this.postId = postId;
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
    
    public Double getAuthorScore() {
        return authorScore;
    }
    
    public void setAuthorScore(Double authorScore) {
        this.authorScore = authorScore;
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
    
    public Integer getVoteCount() {
        return voteCount;
    }
    
    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }
} 