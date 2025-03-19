package com.utcn.contentservice.service;

import com.utcn.contentservice.entity.Postable;
import com.utcn.contentservice.repository.PostableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostableService {

    private final PostableRepository postableRepository;

    @Autowired
    public PostableService(PostableRepository postableRepository) {
        this.postableRepository = postableRepository;
    }

    public List<Postable> getAllPosts() {
        List<Postable> posts = new ArrayList<>();
        postableRepository.findAll().forEach(posts::add);
        return posts.stream()
                .filter(x -> x.getTitle() != null && x.getParent() == null)
                .collect(Collectors.toList());
    }

    public Optional<Postable> getPostById(Integer id) {
        return postableRepository.findById(id);
    }

    public List<Postable> getAllComments() {
        List<Postable> posts = new ArrayList<>();
        postableRepository.findAll().forEach(posts::add);
        return posts.stream()
                .filter(x -> x.getTitle() == null && x.getParent() != null)
                .collect(Collectors.toList());
    }

    public Optional<Postable> getCommentById(Integer id) {
        return postableRepository.findById(id)
                .filter(x -> x.getTitle() == null && x.getParent() != null);
    }

    public List<Postable> getCommentsByPostId(Integer postId) {
        return postableRepository.findById(postId)
                .map(p -> new ArrayList<>(p.getComments()))
                .orElse(new ArrayList<>());
    }

    public Postable addPost(Postable post) {
        return postableRepository.save(post);
    }

    public Postable updatePost(Postable post) {
        return postableRepository.save(post);
    }

    public void deletePost(Integer id) {
        postableRepository.deleteById(id);
    }

    public List<Postable> retrievePostsByUserId(Integer userId) {
        return postableRepository.findByUserIdAndTitleIsNotNullAndParentIsNull(userId);
    }

    public List<Postable> retrievePostsByGroupId(Integer groupId) {
        return postableRepository.findByGroupIdAndTitleIsNotNullAndParentIsNull(groupId);
    }
} 