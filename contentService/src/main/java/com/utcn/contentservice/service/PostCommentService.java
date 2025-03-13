package com.utcn.contentservice.service;

import com.utcn.contentservice.entity.PostComment;
import com.utcn.contentservice.repository.PostCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostCommentService {

    @Autowired
    private PostCommentRepository postCommentRepository;

    public List<PostComment> getAllComments() {
        List<PostComment> comments = new ArrayList<>();
        postCommentRepository.findAll().forEach(comments::add);
        return comments;
    }

    public PostComment getCommentById(Integer id) {
        Optional<PostComment> comment = postCommentRepository.findById(id);
        return comment.orElse(null);
    }

    public List<PostComment> getCommentsByPostId(Integer postId) {
        return postCommentRepository.findByPostId(postId);
    }

    public List<PostComment> getCommentsByCommenterId(Integer commenterId) {
        return postCommentRepository.findByCommenterId(commenterId);
    }

    public PostComment addComment(PostComment comment) {
        return postCommentRepository.save(comment);
    }

    public PostComment updateComment(PostComment comment) {
        return postCommentRepository.save(comment);
    }

    public void deleteComment(Integer id) {
        postCommentRepository.deleteById(id);
    }
} 