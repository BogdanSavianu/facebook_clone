package com.utcn.contentservice.service;

import com.utcn.contentservice.entity.Postable;
import com.utcn.contentservice.entity.PostableVote;
import com.utcn.contentservice.repository.PostableVoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostableVoteServiceTest {

    @Mock
    private PostableVoteRepository voteRepository;

    @InjectMocks
    private PostableVoteService voteService;

    private Postable testPost;
    private PostableVote testVote;
    private Integer testUserId;

    @BeforeEach
    void setUp() {
        testUserId = 1;
        
        // Setup test post
        testPost = new Postable();
        testPost.setId(1);
        testPost.setUserId(2);
        testPost.setTitle("Test Post");
        testPost.setBody("Test Post Body");
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());

        // Setup test vote
        testVote = new PostableVote();
        testVote.setId(1);
        testVote.setPostable(testPost);
        testVote.setUserId(testUserId);
        testVote.setValue(1); // Upvote
        testVote.setCreatedAt(LocalDateTime.now());
        testVote.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getVoteByPostableAndUser_ShouldReturnVote() {
        when(voteRepository.findByPostableAndUserId(testPost, testUserId))
            .thenReturn(Optional.of(testVote));

        Optional<PostableVote> result = voteService.getVoteByPostableAndUser(testPost, testUserId);

        assertTrue(result.isPresent());
        assertEquals(testVote, result.get());
    }

    @Test
    void getVotesByPostable_ShouldReturnAllVotes() {
        List<PostableVote> votes = Arrays.asList(testVote);
        when(voteRepository.findByPostable(testPost)).thenReturn(votes);

        List<PostableVote> result = voteService.getVotesByPostable(testPost);

        assertEquals(1, result.size());
        assertEquals(testVote, result.get(0));
    }

    @Test
    void getVotesByUser_ShouldReturnUserVotes() {
        List<PostableVote> votes = Arrays.asList(testVote);
        when(voteRepository.findByUserId(testUserId)).thenReturn(votes);

        List<PostableVote> result = voteService.getVotesByUser(testUserId);

        assertEquals(1, result.size());
        assertEquals(testVote, result.get(0));
    }

    @Test
    void addVote_ShouldSaveAndReturnVote() {
        when(voteRepository.save(any(PostableVote.class))).thenReturn(testVote);

        PostableVote result = voteService.addVote(testVote);

        assertNotNull(result);
        assertEquals(testVote.getId(), result.getId());
        assertEquals(testVote.getValue(), result.getValue());
    }

    @Test
    void updateVote_ShouldUpdateAndReturnVote() {
        PostableVote updatedVote = testVote;
        updatedVote.setValue(-1); // Change to downvote
        when(voteRepository.save(any(PostableVote.class))).thenReturn(updatedVote);

        PostableVote result = voteService.updateVote(updatedVote);

        assertNotNull(result);
        assertEquals(-1, result.getValue());
    }

    @Test
    void deleteVote_ShouldCallRepository() {
        doNothing().when(voteRepository).delete(any(PostableVote.class));

        voteService.deleteVote(testVote);

        verify(voteRepository, times(1)).delete(testVote);
    }

    @Test
    void getVoteCount_ShouldReturnCorrectCount() {
        when(voteRepository.countByPostable(testPost)).thenReturn(5L);

        long result = voteService.getVoteCount(testPost);

        assertEquals(5L, result);
    }

    @Test
    void deleteVotesByPostable_ShouldCallRepository() {
        doNothing().when(voteRepository).deleteByPostable(any(Postable.class));

        voteService.deleteVotesByPostable(testPost);

        verify(voteRepository, times(1)).deleteByPostable(testPost);
    }
} 