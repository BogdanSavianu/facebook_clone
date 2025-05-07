package com.facebook.reaction.payload.request;

import jakarta.validation.constraints.NotNull;

public class ReactionRequest {
    
    @NotNull
    private Long entityId;
    
    @NotNull
    private String entityType;
    
    @NotNull
    private Boolean isLike;
    
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
    
    public Boolean getIsLike() {
        return isLike;
    }
    
    public void setIsLike(Boolean isLike) {
        this.isLike = isLike;
    }
} 