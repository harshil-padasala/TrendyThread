package com.trendythread.app.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostViewServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private PostViewServiceImpl service;

    private static final String VIEWS_KEY = "REDIS:POST:VIEWS";

    @BeforeEach
    void setUp() {
        service = new PostViewServiceImpl(redisTemplate);
    }

    @Test
    void recordViewIncrementsScoreForPost() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        service.recordView(7);

        verify(zSetOperations).incrementScore(VIEWS_KEY, "7", 1);
    }

    @Test
    void recordViewSwallowsRedisFailure() {
        when(redisTemplate.opsForZSet()).thenThrow(new QueryTimeoutException("redis down"));

        service.recordView(7); // should not throw
    }

    @Test
    void getViewCountReturnsScoreAsLong() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.score(VIEWS_KEY, "7")).thenReturn(42.0);

        assertEquals(42L, service.getViewCount(7));
    }

    @Test
    void getViewCountReturnsZeroWhenNeverViewed() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.score(VIEWS_KEY, "7")).thenReturn(null);

        assertEquals(0L, service.getViewCount(7));
    }

    @Test
    void getViewCountReturnsZeroWhenRedisUnavailable() {
        when(redisTemplate.opsForZSet()).thenThrow(new QueryTimeoutException("redis down"));

        assertEquals(0L, service.getViewCount(7));
    }

    @Test
    void getTrendingPostIdsReturnsIdsInRankOrder() {
        Set<String> members = new LinkedHashSet<>(List.of("3", "1", "9"));
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRange(VIEWS_KEY, 0, 2L)).thenReturn(members);

        List<Integer> result = service.getTrendingPostIds(3);

        assertEquals(List.of(3, 1, 9), result);
    }

    @Test
    void getTrendingPostIdsReturnsEmptyWhenNoViewsRecorded() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRange(VIEWS_KEY, 0, 9L)).thenReturn(null);

        assertTrue(service.getTrendingPostIds(10).isEmpty());
    }

    @Test
    void getTrendingPostIdsReturnsEmptyWhenRedisUnavailable() {
        when(redisTemplate.opsForZSet()).thenThrow(new QueryTimeoutException("redis down"));

        assertTrue(service.getTrendingPostIds(10).isEmpty());
    }
}
