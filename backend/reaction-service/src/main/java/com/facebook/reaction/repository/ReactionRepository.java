package com.facebook.reaction.repository;

import com.facebook.reaction.model.EntityType;
import com.facebook.reaction.model.Reaction;
import com.facebook.reaction.model.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    
    Optional<Reaction> findByUserIdAndEntityTypeAndEntityId(Long userId, EntityType entityType, Long entityId);
    
    List<Reaction> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
    
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.entityType = ?1 AND r.entityId = ?2 AND r.reactionType = ?3")
    int countByEntityTypeAndEntityIdAndReactionType(EntityType entityType, Long entityId, ReactionType reactionType);
    
    void deleteByUserIdAndEntityTypeAndEntityId(Long userId, EntityType entityType, Long entityId);
} 