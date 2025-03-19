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
        testPost.setUserId(2); // Different from voter
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
        // Arrange
        when(voteRepository.findByPostableAndUserId(testPost, testUserId))
            .thenReturn(Optional.of(testVote));

        // Act
        Optional<PostableVote> result = voteService.getVoteByPostableAndUser(testPost, testUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testVote, result.get());
    }

    @Test
    void getVotesByPostable_ShouldReturnAllVotes() {
        // Arrange
        List<PostableVote> votes = Arrays.asList(testVote);
        when(voteRepository.findByPostable(testPost)).thenReturn(votes);

        // Act
        List<PostableVote> result = voteService.getVotesByPostable(testPost);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testVote, result.get(0));
    }

    @Test
    void getVotesByUser_ShouldReturnUserVotes() {
        // Arrange
        List<PostableVote> votes = Arrays.asList(testVote);
        when(voteRepository.findByUserId(testUserId)).thenReturn(votes);

        // Act
        List<PostableVote> result = voteService.getVotesByUser(testUserId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testVote, result.get(0));
    }

    @Test
    void addVote_ShouldSaveAndReturnVote() {
        // Arrange
        when(voteRepository.save(any(PostableVote.class))).thenReturn(testVote);

        // Act
        PostableVote result = voteService.addVote(testVote);

        // Assert
        assertNotNull(result);
        assertEquals(testVote.getId(), result.getId());
        assertEquals(testVote.getValue(), result.getValue());
        verify(voteRepository, times(1)).save(any(PostableVote.class));
    }

    @Test
    void updateVote_ShouldUpdateAndReturnVote() {
        // Arrange
        PostableVote updatedVote = testVote;
        updatedVote.setValue(-1); // Change to downvote
        when(voteRepository.save(any(PostableVote.class))).thenReturn(updatedVote);

        // Act
        PostableVote result = voteService.updateVote(updatedVote);

        // Assert
        assertNotNull(result);
        assertEquals(-1, result.getValue());
        verify(voteRepository, times(1)).save(any(PostableVote.class));
    }

    @Test
    void deleteVote_ShouldCallRepository() {
        // Arrange
        doNothing().when(voteRepository).delete(any(PostableVote.class));

        // Act
        voteService.deleteVote(testVote);

        // Assert
        verify(voteRepository, times(1)).delete(testVote);
    }

    @Test
    void getVoteCount_ShouldReturnCorrectCount() {
        // Arrange
        when(voteRepository.countByPostable(testPost)).thenReturn(5L);

        // Act
        long result = voteService.getVoteCount(testPost);

        // Assert
        assertEquals(5L, result);
    }

    @Test
    void deleteVotesByPostable_ShouldCallRepository() {
        // Arrange
        doNothing().when(voteRepository).deleteByPostable(any(Postable.class));

        // Act
        voteService.deleteVotesByPostable(testPost);

        // Assert
        verify(voteRepository, times(1)).deleteByPostable(testPost);
    }
} 