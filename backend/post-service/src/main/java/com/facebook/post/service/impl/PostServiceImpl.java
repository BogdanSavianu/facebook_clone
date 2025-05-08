package com.facebook.post.service.impl;

import com.facebook.post.model.Post;
import com.facebook.post.model.PostStatus;
import com.facebook.post.model.Tag;
import com.facebook.post.payload.PostDTO;
import com.facebook.post.payload.TagDTO;
import com.facebook.post.payload.UserDTO;
import com.facebook.post.payload.request.PostRequest;
import com.facebook.post.repository.PostRepository;
import com.facebook.post.service.PostService;
import com.facebook.post.service.TagService;
import com.facebook.post.service.UserService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${comment-service.url}")
    private String commentServiceUrl;

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private UserService userService;

    @Override
    public Page<PostDTO> getAllPosts(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        }
        return postRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<PostDTO> getAllPostsFiltered(Pageable pageable, String tagName, String titleSearch, Long authorId) {
        if (pageable.getSort().isUnsorted()) {
            pageable = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        }

        Specification<Post> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(tagName)) {
                Join<Post, Tag> tagsJoin = root.join("tags");
                predicates.add(criteriaBuilder.equal(tagsJoin.get("name"), tagName));
            }

            if (StringUtils.hasText(titleSearch)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + titleSearch.toLowerCase() + "%"));
            }

            if (authorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("authorId"), authorId));
            }
            

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return postRepository.findAll(spec, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public PostDTO getPostById(Long id) {
        return postRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    @Override
    @Transactional
    public PostDTO createPost(Long authorId, PostRequest postRequest) {
        Post post = new Post();
        post.setAuthorId(authorId);
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setImageUrl(postRequest.getImageUrl());
        post.setCreatedAt(LocalDateTime.now());
        post.setStatus(PostStatus.JUST_POSTED);
        post.setVoteCount(0);
        
        if (postRequest.getTagNames() != null && !postRequest.getTagNames().isEmpty()) {
            Set<Tag> tags = tagService.getOrCreateTagsByNames(postRequest.getTagNames());
            post.setTags(tags);
        }
        
        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    @Override
    @Transactional
    public PostDTO updatePost(Long id, Long userId, PostRequest postRequest) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        
        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("User not authorized to update this post");
        }
        
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setImageUrl(postRequest.getImageUrl());
        
        if (postRequest.getTagNames() != null) {
            Set<Tag> tags = tagService.getOrCreateTagsByNames(postRequest.getTagNames());
            post.setTags(tags);
        }
        
        Post updatedPost = postRepository.save(post);
        return convertToDTO(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        
        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("User not authorized to delete this post");
        }
        
        postRepository.delete(post);
    }

    @Override
    public List<PostDTO> getPostsByAuthor(Long authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PostDTO> searchPostsByTitle(String query, Pageable pageable) {
        return postRepository.searchByTitle(query, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<PostDTO> getPostsByTag(String tagName, Pageable pageable) {
        return postRepository.findByTagName(tagName, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public PostDTO updatePostStatus(Long id, Long userId, PostStatus status) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        
        post.setStatus(status);
        Post updatedPost = postRepository.save(post);
        return convertToDTO(updatedPost);
    }

    @Override
    @Transactional
    public PostDTO votePost(Long id, Long userId, boolean upvote) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        
        if (post.getAuthorId().equals(userId)) {
            throw new RuntimeException("User cannot vote on their own post");
        }

        logger.warn("votePost in PostServiceImpl was called. This might be redundant if reaction-service handles all votes.");
        return convertToDTO(post);
    }

    @Override
    @Transactional
    public void updatePostVoteCount(Long postId, int voteCount) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        post.setVoteCount(voteCount);
        postRepository.save(post);
        logger.info("Updated vote count for post {} to {}", postId, voteCount);
    }
    
    private PostDTO convertToDTO(Post post) {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(post.getId());
        postDTO.setAuthorId(post.getAuthorId());
        
        try {
            UserDTO author = userService.getUserById(post.getAuthorId());
            postDTO.setAuthorId(author.getId());
            postDTO.setAuthorUsername(author.getUsername());
        } catch (Exception e) {
            logger.error("Failed to fetch author details for post {}: {}", post.getId(), e.getMessage());
            postDTO.setAuthorUsername("Unknown");
        }
        
        postDTO.setTitle(post.getTitle());
        postDTO.setContent(post.getContent());
        postDTO.setCreatedAt(post.getCreatedAt());
        postDTO.setImageUrl(post.getImageUrl());
        postDTO.setVoteCount(post.getVoteCount());
        postDTO.setStatus(post.getStatus() != null ? post.getStatus() : PostStatus.JUST_POSTED);
        
        Set<TagDTO> tagDTOs = post.getTags().stream()
                .map(tag -> {
                    TagDTO tagDTO = new TagDTO();
                    tagDTO.setId(tag.getId());
                    tagDTO.setName(tag.getName());
                    return tagDTO;
                })
                .collect(Collectors.toSet());
        postDTO.setTags(tagDTOs);
        
        try {
            String url = commentServiceUrl + "/comments/internal/post/" + post.getId() + "/count";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                 Object countObj = response.getBody().get("commentCount");
                 if (countObj instanceof Number) {
                     postDTO.setCommentCount(((Number) countObj).intValue());
                 } else {
                      logger.warn("Received unexpected type for commentCount from comment-service for post {}: {}", post.getId(), countObj != null ? countObj.getClass().getName() : "null");
                      postDTO.setCommentCount(0);
                 }

            } else {
                 logger.warn("Failed to fetch comment count for post {}. Status: {}", post.getId(), response.getStatusCode());
                 postDTO.setCommentCount(0);
            }
        } catch (Exception e) {
            logger.error("Error calling comment-service for comment count for post {}: {}", post.getId(), e.getMessage());
            postDTO.setCommentCount(0);
        }

        return postDTO;
    }
} 