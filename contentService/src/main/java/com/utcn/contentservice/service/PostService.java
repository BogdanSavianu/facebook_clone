package com.utcn.contentservice.service;

import com.utcn.contentservice.entity.Post;
import com.utcn.contentservice.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public List<Post> retrievePosts() {
        List<Post> posts = new ArrayList<>();
        postRepository.findAll().forEach(posts::add);
        return posts;
    }

    public Post retrievePostById(Integer id) {
        Optional<Post> post = postRepository.findById(id);
        return post.orElse(null);
    }

    public List<Post> retrievePostsByUserId(Integer userId) {
        return postRepository.findByUserId(userId);
    }

    public List<Post> retrievePostsByGroupId(Integer groupId) {
        return postRepository.findByGroupId(groupId);
    }

    public Post addPost(Post post) {
        return postRepository.save(post);
    }

    public Post updatePost(Post post) {
        return postRepository.save(post);
    }

    public void deletePostById(Integer id) {
        postRepository.deleteById(id);
    }
} 