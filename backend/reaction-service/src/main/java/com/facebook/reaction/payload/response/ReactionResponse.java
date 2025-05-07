package com.facebook.reaction.payload.response;

public class ReactionResponse {
    
    private Long entityId;
    private String entityType;
    private int likeCount;
    private int dislikeCount;
    private int totalScore;
    private String userReaction;
    
    private Long reactionId;
    private Long performingUserId;
    private String eventTimestamp;
    private String message;
    private Long authorId;
    private Double authorScore;
    private Double performingUserScore;
    
    public ReactionResponse() {
    }
    
    public ReactionResponse(Long entityId, String entityType, int likeCount, int dislikeCount, String userReaction) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.totalScore = likeCount - dislikeCount;
        this.userReaction = userReaction;
    }
    
    public ReactionResponse(Long reactionId, Long performingUserId, String userReaction,
                            String eventTimestamp, String message, Long entityId, String entityType,
                            int likeCount, int dislikeCount, Long authorId, Double authorScore) {
        this.reactionId = reactionId;
        this.performingUserId = performingUserId;
        this.userReaction = userReaction;
        this.eventTimestamp = eventTimestamp;
        this.message = message;
        this.entityId = entityId;
        this.entityType = entityType;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.totalScore = likeCount - dislikeCount;
        this.authorId = authorId;
        this.authorScore = authorScore;
    }
    
    public ReactionResponse(Long reactionId, Long performingUserId, String userReaction,
                            String eventTimestamp, String message, Long entityId, String entityType,
                            int likeCount, int dislikeCount, Long authorId, Double authorScore,
                            Double performingUserScore) {
        this.reactionId = reactionId;
        this.performingUserId = performingUserId;
        this.userReaction = userReaction;
        this.eventTimestamp = eventTimestamp;
        this.message = message;
        this.entityId = entityId;
        this.entityType = entityType;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.totalScore = likeCount - dislikeCount;
        this.authorId = authorId;
        this.authorScore = authorScore;
        this.performingUserScore = performingUserScore;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public int getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
        this.totalScore = this.likeCount - this.dislikeCount;
    }
    
    public int getDislikeCount() {
        return dislikeCount;
    }
    
    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
        this.totalScore = this.likeCount - this.dislikeCount;
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public String getUserReaction() {
        return userReaction;
    }
    
    public void setUserReaction(String userReaction) {
        this.userReaction = userReaction;
    }

    public Long getReactionId() { return reactionId; }
    public void setReactionId(Long reactionId) { this.reactionId = reactionId; }

    public Long getPerformingUserId() { return performingUserId; }
    public void setPerformingUserId(Long performingUserId) { this.performingUserId = performingUserId; }

    public String getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(String eventTimestamp) { this.eventTimestamp = eventTimestamp; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public Double getAuthorScore() { return authorScore; }
    public void setAuthorScore(Double authorScore) { this.authorScore = authorScore; }

    public Double getPerformingUserScore() { return performingUserScore; }
    public void setPerformingUserScore(Double performingUserScore) { this.performingUserScore = performingUserScore; }
} 