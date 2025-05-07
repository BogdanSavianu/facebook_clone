package com.facebook.reaction.service.impl;

import com.facebook.reaction.model.EntityType;
import com.facebook.reaction.model.Reaction;
import com.facebook.reaction.model.ReactionType;
import com.facebook.reaction.payload.internal.CommentAuthorDTO;
import com.facebook.reaction.payload.internal.PostAuthorDTO;
import com.facebook.reaction.payload.internal.UserDTO;
import com.facebook.reaction.payload.response.ReactionResponse;
import com.facebook.reaction.repository.ReactionRepository;
import com.facebook.reaction.service.ReactionService;
import com.facebook.reaction.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class ReactionServiceImpl implements ReactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReactionServiceImpl.class);
    
    private static final double OWNER_POST_LIKED_SCORE = 2.5;
    private static final double OWNER_POST_DISLIKED_SCORE = -1.5;
    private static final double OWNER_COMMENT_LIKED_SCORE = 5.0;
    private static final double OWNER_COMMENT_DISLIKED_SCORE = -2.5;
    private static final double REACTOR_COMMENT_DISLIKED_PENALTY = -1.5;
    
    @Autowired
    private ReactionRepository reactionRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${post-service.url:http://localhost:8083}")
    private String postServiceUrl;
    
    @Value("${comment-service.url:http://localhost:8084}")
    private String commentServiceUrl;
    
    @Override
    @Transactional
    public ReactionResponse reactToEntity(Long performingUserId, EntityType entityType, Long entityId, ReactionType newReactionTypeFromRequest) {
        logger.info("User {} reacting with {} to {}:{}", performingUserId, newReactionTypeFromRequest, entityType, entityId);
        
        Optional<Reaction> existingReactionOpt = reactionRepository.findByUserIdAndEntityTypeAndEntityId(performingUserId, entityType, entityId);
        
        Long ownerId = getEntityOwnerId(entityType, entityId);
        if (ownerId == null) {
            logger.warn("Could not determine owner for {} {}. Score changes for owner will not be applied.", entityType, entityId);
        }
        
        if (ownerId != null && ownerId.equals(performingUserId)) {
            logger.warn("User {} attempted to react to their own {} {}. Action denied.", performingUserId, entityType, entityId);
            int currentLikes = reactionRepository.countByEntityTypeAndEntityIdAndReactionType(entityType, entityId, ReactionType.LIKE);
            int currentDislikes = reactionRepository.countByEntityTypeAndEntityIdAndReactionType(entityType, entityId, ReactionType.DISLIKE);
            Double currentAuthorScore = null;
            if (ownerId != null) {
                UserDTO authorDto = userService.fetchUserById(ownerId);
                if (authorDto != null) currentAuthorScore = authorDto.getUserScore();
            }
            return new ReactionResponse(null, performingUserId, null, LocalDateTime.now().toString(), "Cannot react to own content.", 
                                    entityId, entityType.name(), currentLikes, currentDislikes, ownerId, currentAuthorScore);
        }

        Reaction savedReaction = null; 
        String message;
        ReactionType finalReactionTypeForUserResponse = newReactionTypeFromRequest;
        LocalDateTime eventTimestamp = LocalDateTime.now();

        if (existingReactionOpt.isPresent()) {
            Reaction reaction = existingReactionOpt.get();
            ReactionType oldReactionType = reaction.getReactionType();

            if (oldReactionType == newReactionTypeFromRequest) {
                logger.info("User {} un-reacting from {}:{} (was {})", performingUserId, entityType, entityId, oldReactionType);
                reactionRepository.delete(reaction);
                finalReactionTypeForUserResponse = null; 
                message = "Reaction removed.";
                eventTimestamp = reaction.getUpdatedAt(); 

                if (oldReactionType == ReactionType.LIKE) {
                    if (ownerId != null) {
                        double scoreChange = (entityType == EntityType.POST) ? -OWNER_POST_LIKED_SCORE : -OWNER_COMMENT_LIKED_SCORE;
                        userService.updateUserScore(ownerId, scoreChange);
                    }
                } else if (oldReactionType == ReactionType.DISLIKE) {
                    if (ownerId != null) {
                        double scoreChange = (entityType == EntityType.POST) ? -OWNER_POST_DISLIKED_SCORE : -OWNER_COMMENT_DISLIKED_SCORE;
                        userService.updateUserScore(ownerId, scoreChange);
                    }
                    if (entityType == EntityType.COMMENT) {
                        userService.updateUserScore(performingUserId, -REACTOR_COMMENT_DISLIKED_PENALTY);
                    }
                }
            } else {
                logger.info("User {} changed reaction from {} to {} for {}:{}", performingUserId, oldReactionType, newReactionTypeFromRequest, entityType, entityId);
                reaction.setReactionType(newReactionTypeFromRequest);
                reaction.setUpdatedAt(eventTimestamp);
                savedReaction = reactionRepository.save(reaction);
                message = "Reaction updated.";

                if (oldReactionType == ReactionType.LIKE) {
                    if (ownerId != null) {
                        double scoreChange = (entityType == EntityType.POST) ? -OWNER_POST_LIKED_SCORE : -OWNER_COMMENT_LIKED_SCORE;
                        userService.updateUserScore(ownerId, scoreChange);
                    }
                } else if (oldReactionType == ReactionType.DISLIKE) {
                    if (ownerId != null) {
                        double scoreChange = (entityType == EntityType.POST) ? -OWNER_POST_DISLIKED_SCORE : -OWNER_COMMENT_DISLIKED_SCORE;
                        userService.updateUserScore(ownerId, scoreChange);
                    }
                    if (entityType == EntityType.COMMENT) {
                        userService.updateUserScore(performingUserId, -REACTOR_COMMENT_DISLIKED_PENALTY);
                    }
                }
                
                if (newReactionTypeFromRequest == ReactionType.LIKE) {
                    if (ownerId != null) {
                        double scoreChange = (entityType == EntityType.POST) ? OWNER_POST_LIKED_SCORE : OWNER_COMMENT_LIKED_SCORE;
                        userService.updateUserScore(ownerId, scoreChange);
                    }
                } else { 
                    if (ownerId != null) {
                        double scoreChange = (entityType == EntityType.POST) ? OWNER_POST_DISLIKED_SCORE : OWNER_COMMENT_DISLIKED_SCORE;
                        userService.updateUserScore(ownerId, scoreChange);
                    }
                    if (entityType == EntityType.COMMENT) {
                        userService.updateUserScore(performingUserId, REACTOR_COMMENT_DISLIKED_PENALTY);
                    }
                }
            }
        } else {
            logger.info("User {} creating new reaction {} for {}:{}", performingUserId, newReactionTypeFromRequest, entityType, entityId);
            Reaction newReactionRecord = new Reaction(performingUserId, newReactionTypeFromRequest, entityType, entityId);
            newReactionRecord.setUpdatedAt(eventTimestamp); 
            savedReaction = reactionRepository.save(newReactionRecord);
            message = "Reaction added.";

            if (newReactionTypeFromRequest == ReactionType.LIKE) {
                if (ownerId != null) {
                    double scoreChange = (entityType == EntityType.POST) ? OWNER_POST_LIKED_SCORE : OWNER_COMMENT_LIKED_SCORE;
                    userService.updateUserScore(ownerId, scoreChange);
                }
            } else { 
                if (ownerId != null) {
                    double scoreChange = (entityType == EntityType.POST) ? OWNER_POST_DISLIKED_SCORE : OWNER_COMMENT_DISLIKED_SCORE;
                    userService.updateUserScore(ownerId, scoreChange);
                }
                if (entityType == EntityType.COMMENT) {
                    userService.updateUserScore(performingUserId, REACTOR_COMMENT_DISLIKED_PENALTY);
                }
            }
        }

        Double finalAuthorScore = null;
        if (ownerId != null) {
            UserDTO authorDTO = userService.fetchUserById(ownerId);
            if (authorDTO != null) {
                finalAuthorScore = authorDTO.getUserScore();
            }
        }
        
        int finalLikeCount = reactionRepository.countByEntityTypeAndEntityIdAndReactionType(entityType, entityId, ReactionType.LIKE);
        int finalDislikeCount = reactionRepository.countByEntityTypeAndEntityIdAndReactionType(entityType, entityId, ReactionType.DISLIKE);
        updateEntityVoteCount(entityType, entityId, finalLikeCount - finalDislikeCount);

        String finalUserReactionString = finalReactionTypeForUserResponse != null ? finalReactionTypeForUserResponse.name() : null;
        return new ReactionResponse(
            savedReaction != null ? savedReaction.getId() : null,
            performingUserId,
            finalUserReactionString,
            eventTimestamp.toString(),
            message,
            entityId,
            entityType.name(),
            finalLikeCount,
            finalDislikeCount,
            ownerId,
            finalAuthorScore,
            null
        );
    }
    
    @Override
    @Transactional
    public ReactionResponse removeReaction(Long performingUserId, EntityType entityType, Long entityId) {
        logger.info("User {} is removing reaction to {}: {}", performingUserId, entityType, entityId);
        Optional<Reaction> existingReactionOpt = reactionRepository.findByUserIdAndEntityTypeAndEntityId(performingUserId, entityType, entityId);

        String message = "No reaction found to remove.";
        Long ownerId = getEntityOwnerId(entityType, entityId);
        Double finalAuthorScore = null;
        LocalDateTime removalTimestamp = LocalDateTime.now();
        ReactionType removedReactionType = null;

        if (existingReactionOpt.isPresent()) {
            Reaction reaction = existingReactionOpt.get();
            removedReactionType = reaction.getReactionType();
            
            reactionRepository.delete(reaction);
            message = "Reaction removed successfully.";
            logger.info("User {} successfully removed {} reaction to {}: {}", performingUserId, removedReactionType, entityType, entityId);

            if (removedReactionType == ReactionType.LIKE) {
                if (ownerId != null) {
                    double scoreChange = (entityType == EntityType.POST) ? -OWNER_POST_LIKED_SCORE : -OWNER_COMMENT_LIKED_SCORE;
                    userService.updateUserScore(ownerId, scoreChange);
                }
            } else if (removedReactionType == ReactionType.DISLIKE) {
                if (ownerId != null) {
                    double scoreChange = (entityType == EntityType.POST) ? -OWNER_POST_DISLIKED_SCORE : -OWNER_COMMENT_DISLIKED_SCORE;
                    userService.updateUserScore(ownerId, scoreChange);
                }
                if (entityType == EntityType.COMMENT) {
                    userService.updateUserScore(performingUserId, -REACTOR_COMMENT_DISLIKED_PENALTY);
                }
            }
            
            if (ownerId != null) {
                 UserDTO authorDTO = userService.fetchUserById(ownerId);
                if (authorDTO != null) {
                    finalAuthorScore = authorDTO.getUserScore();
                }
            }
        } else {
            logger.warn("User {} attempted to remove a non-existent reaction for {}:{}.", performingUserId, entityType, entityId);
        }
        
        int finalLikeCount = reactionRepository.countByEntityTypeAndEntityIdAndReactionType(entityType, entityId, ReactionType.LIKE);
        int finalDislikeCount = reactionRepository.countByEntityTypeAndEntityIdAndReactionType(entityType, entityId, ReactionType.DISLIKE);
        updateEntityVoteCount(entityType, entityId, finalLikeCount - finalDislikeCount);

        return new ReactionResponse(
            null,
            performingUserId,
            null,
            removalTimestamp.toString(),
            message,
            entityId,
            entityType.name(),
            finalLikeCount,
            finalDislikeCount,
            ownerId,
            finalAuthorScore,
            null
        );
    }
    
    @Override
    public ReactionResponse getReactionStats(EntityType entityType, Long entityId, Long currentUserId, Long authorIdIfKnown, Double authorScoreIfKnown) {
        int likeCount = reactionRepository.countByEntityTypeAndEntityIdAndReactionType(entityType, entityId, ReactionType.LIKE);
        int dislikeCount = reactionRepository.countByEntityTypeAndEntityIdAndReactionType(entityType, entityId, ReactionType.DISLIKE);
        
        ReactionType usersCurrentReactionType = null;
        Long usersReactionId = null; 
        String reactionEventTimestamp = null;

        if (currentUserId != null) {
            Optional<Reaction> userReactionObj = reactionRepository.findByUserIdAndEntityTypeAndEntityId(currentUserId, entityType, entityId);
            if (userReactionObj.isPresent()) {
                Reaction r = userReactionObj.get();
                usersCurrentReactionType = r.getReactionType();
                usersReactionId = r.getId();
                if (r.getUpdatedAt() != null) { 
                    reactionEventTimestamp = r.getUpdatedAt().toString();
                }
            }
        }
        
        updateEntityVoteCount(entityType, entityId, likeCount - dislikeCount);

        String usersCurrentReactionString = usersCurrentReactionType != null ? usersCurrentReactionType.name() : null;
        return new ReactionResponse(
                usersReactionId,
                currentUserId,
                usersCurrentReactionString,
                reactionEventTimestamp,
                "Current entity reaction statistics.",
                entityId,
                entityType.name(),
                likeCount,
                dislikeCount,
                authorIdIfKnown,
                authorScoreIfKnown
        );
    }
    
    private Long getEntityOwnerId(EntityType entityType, Long entityId) {
        String url;
        try {
            if (entityType == EntityType.POST) {
                url = postServiceUrl + "/api/posts/public/" + entityId;
                logger.debug("Fetching post authorId from URL: {}", url);
                PostAuthorDTO post = restTemplate.getForObject(url, PostAuthorDTO.class);
                return post != null ? post.authorId() : null;
            } else if (entityType == EntityType.COMMENT) {
                url = commentServiceUrl + "/api/comments/public/" + entityId;
                logger.debug("Fetching comment authorId from URL: {}", url);
                CommentAuthorDTO comment = restTemplate.getForObject(url, CommentAuthorDTO.class);
                return comment != null ? comment.authorId() : null;
            }
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Entity not found when fetching ownerId for {} {}: {}", entityType, entityId, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error fetching ownerId for {} {}: {}", entityType, entityId, e.getMessage(), e);
            return null;
        }
        return null;
    }
    
    private void updateEntityVoteCount(EntityType entityType, Long entityId, int voteCount) {
        String url;
        Map<String, Integer> body = Map.of("voteCount", voteCount);
        try {
            if (entityType == EntityType.POST) {
                url = postServiceUrl + "/api/posts/internal/" + entityId + "/vote-count";
                logger.debug("Updating vote count for POST {} to {} via URL: {}", entityId, voteCount, url);
                restTemplate.put(url, body);
                logger.info("Successfully updated vote count for POST {} to {}", entityId, voteCount);
            } else if (entityType == EntityType.COMMENT) {
                url = commentServiceUrl + "/api/comments/internal/" + entityId + "/vote-count";
                logger.debug("Updating vote count for COMMENT {} to {} via URL: {}", entityId, voteCount, url);
                restTemplate.put(url, body);
                logger.info("Successfully updated vote count for COMMENT {} to {}", entityId, voteCount);
            }
        } catch (HttpClientErrorException e) {
            logger.error("HttpClientError when updating vote count for {} {}: {} - {}", entityType, entityId, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Failed to update vote count for {} {}: {}", entityType, entityId, e.getMessage(), e);
        }
    }
} 