package com.trendythread.app.services.impl;

import com.trendythread.app.dto.LikeStatusDto;
import com.trendythread.app.entities.Blogger;
import com.trendythread.app.entities.Post;
import com.trendythread.app.exceptions.ResourceNotFoundException;
import com.trendythread.app.repositories.BloggersRepository;
import com.trendythread.app.repositories.PostLikeRepository;
import com.trendythread.app.repositories.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private BloggersRepository bloggersRepository;

    @InjectMocks
    private LikeServiceImpl likeService;

    @Test
    void toggleLikeLikesWhenNotAlreadyLiked() {
        when(postRepository.existsById(5)).thenReturn(true);
        when(postLikeRepository.existsByPostIdAndBloggerEmail(5, "reader@example.com")).thenReturn(false);
        when(postRepository.getReferenceById(5)).thenReturn(new Post());
        when(bloggersRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(new Blogger()));
        when(postLikeRepository.countByPostId(5)).thenReturn(1L);

        LikeStatusDto result = likeService.toggleLike(5, "reader@example.com");

        assertTrue(result.isLiked());
        assertEquals(1L, result.getLikeCount());
        verify(postLikeRepository).save(org.mockito.ArgumentMatchers.any());
        verify(postLikeRepository, never()).deleteByPostIdAndBloggerEmail(5, "reader@example.com");
    }

    @Test
    void toggleLikeUnlikesWhenAlreadyLiked() {
        when(postRepository.existsById(5)).thenReturn(true);
        when(postLikeRepository.existsByPostIdAndBloggerEmail(5, "reader@example.com")).thenReturn(true);
        when(postLikeRepository.countByPostId(5)).thenReturn(0L);

        LikeStatusDto result = likeService.toggleLike(5, "reader@example.com");

        assertFalse(result.isLiked());
        assertEquals(0L, result.getLikeCount());
        verify(postLikeRepository).deleteByPostIdAndBloggerEmail(5, "reader@example.com");
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void toggleLikeOnMissingPostThrows() {
        when(postRepository.existsById(999)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> likeService.toggleLike(999, "reader@example.com"));
    }

    @Test
    void getLikeStatusForAnonymousUserIsNeverLiked() {
        when(postRepository.existsById(5)).thenReturn(true);
        when(postLikeRepository.countByPostId(5)).thenReturn(3L);

        LikeStatusDto result = likeService.getLikeStatus(5, null);

        assertFalse(result.isLiked());
        assertEquals(3L, result.getLikeCount());
        verify(postLikeRepository, never()).existsByPostIdAndBloggerEmail(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void getLikeStatusForAuthenticatedUserReflectsExistingLike() {
        when(postRepository.existsById(5)).thenReturn(true);
        when(postLikeRepository.existsByPostIdAndBloggerEmail(5, "reader@example.com")).thenReturn(true);
        when(postLikeRepository.countByPostId(5)).thenReturn(3L);

        LikeStatusDto result = likeService.getLikeStatus(5, "reader@example.com");

        assertTrue(result.isLiked());
        assertEquals(3L, result.getLikeCount());
    }

    @Test
    void getLikeStatusOnMissingPostThrows() {
        when(postRepository.existsById(999)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> likeService.getLikeStatus(999, null));
    }
}
