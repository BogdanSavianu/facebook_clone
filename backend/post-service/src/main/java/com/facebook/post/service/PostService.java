package com.facebook.post.service;

import com.facebook.post.model.PostStatus;
import com.facebook.post.payload.PostDTO;
import com.facebook.post.payload.request.PostRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {
    Page<PostDTO> getAllPosts(Pageable pageable);
    
    Page<PostDTO> getAllPostsFiltered(Pageable pageable, String tagName, String titleSearch, Long authorId);
    
    PostDTO getPostById(Long id);
    
    PostDTO createPost(Long authorId, PostRequest postRequest);
    
    PostDTO updatePost(Long id, Long userId, PostRequest postRequest);
    
    void deletePost(Long id, Long userId);
    
    List<PostDTO> getPostsByAuthor(Long authorId);
    
    Page<PostDTO> searchPostsByTitle(String query, Pageable pageable);
    
    Page<PostDTO> getPostsByTag(String tagName, Pageable pageable);
    
    PostDTO updatePostStatus(Long id, Long userId, PostStatus status);
    
    PostDTO votePost(Long id, Long userId, boolean upvote);
    
    void updatePostVoteCount(Long postId, int voteCount);
} 