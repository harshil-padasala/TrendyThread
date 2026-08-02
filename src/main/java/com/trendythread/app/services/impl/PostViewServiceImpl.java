package com.trendythread.app.services.impl;

import com.trendythread.app.services.PostViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostViewServiceImpl implements PostViewService {

    private static final String VIEWS_KEY = "REDIS:POST:VIEWS";

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public PostViewServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void recordView(Integer postId) {
        try {
            redisTemplate.opsForZSet().incrementScore(VIEWS_KEY, postId.toString(), 1);
        } catch (DataAccessException ex) {
            log.warn("PostViewService.recordView - Redis unavailable for postId={}", postId, ex);
        }
    }

    @Override
    public long getViewCount(Integer postId) {
        try {
            Double score = redisTemplate.opsForZSet().score(VIEWS_KEY, postId.toString());
            return score == null ? 0L : score.longValue();
        } catch (DataAccessException ex) {
            log.warn("PostViewService.getViewCount - Redis unavailable for postId={}", postId, ex);
            return 0L;
        }
    }

    @Override
    public List<Integer> getTrendingPostIds(int limit) {
        try {
            Set<String> members = redisTemplate.opsForZSet().reverseRange(VIEWS_KEY, 0, limit - 1L);
            if (members == null) {
                return List.of();
            }
            return members.stream().map(Integer::parseInt).collect(Collectors.toList());
        } catch (DataAccessException ex) {
            log.warn("PostViewService.getTrendingPostIds - Redis unavailable", ex);
            return List.of();
        }
    }
}
