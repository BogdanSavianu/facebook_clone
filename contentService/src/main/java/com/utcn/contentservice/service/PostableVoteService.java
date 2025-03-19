package com.utcn.contentservice.service;

import com.utcn.contentservice.entity.Postable;
import com.utcn.contentservice.entity.PostableVote;
import com.utcn.contentservice.repository.PostableVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostableVoteService {

    @Autowired
    private PostableVoteRepository voteRepository;

    public Optional<PostableVote> getVoteByPostableAndUser(Postable post, Integer userId) {
        return voteRepository.findByPostableAndUserId(post, userId);
    }

    public List<PostableVote> getVotesByPostable(Postable post) {
        return voteRepository.findByPostable(post);
    }

    public List<PostableVote> getVotesByUser(Integer userId) {
        return voteRepository.findByUserId(userId);
    }

    public PostableVote addVote(PostableVote vote) {
        return voteRepository.save(vote);
    }

    public PostableVote updateVote(PostableVote vote) {
        return voteRepository.save(vote);
    }

    public void deleteVote(PostableVote vote) {
        voteRepository.delete(vote);
    }

    public long getVoteCount(Postable post) {
        return voteRepository.countByPostable(post);
    }

    public void deleteVotesByPostable(Postable post) {
        voteRepository.deleteByPostable(post);
    }
} 