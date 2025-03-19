package com.utcn.contentservice.repository;

import com.utcn.contentservice.entity.Postable;
import com.utcn.contentservice.entity.PostableVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostableVoteRepository extends JpaRepository<PostableVote, Integer> {
    Optional<PostableVote> findByPostableAndUserId(Postable post, Integer userId);

    List<PostableVote> findByPostable(Postable post);

    List<PostableVote> findByUserId(Integer userId);

    long countByPostable(Postable post);

    void deleteByPostable(Postable post);
} 