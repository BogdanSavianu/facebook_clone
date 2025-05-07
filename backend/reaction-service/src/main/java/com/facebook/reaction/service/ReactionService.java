package com.facebook.reaction.service;

import com.facebook.reaction.model.EntityType;
import com.facebook.reaction.model.ReactionType;
import com.facebook.reaction.payload.response.ReactionResponse;

public interface ReactionService {
    
    ReactionResponse reactToEntity(Long userId, EntityType entityType, Long entityId, ReactionType reactionType);
    
    ReactionResponse removeReaction(Long userId, EntityType entityType, Long entityId);
    
    ReactionResponse getReactionStats(EntityType entityType, Long entityId, Long currentUserId, Long authorIdIfKnown, Double authorScoreIfKnown);
} 