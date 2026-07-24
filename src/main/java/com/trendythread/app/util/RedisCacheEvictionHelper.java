package com.trendythread.app.util;

import com.trendythread.app.constants.IRedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RedisCacheEvictionHelper {

    private final RedisCacheSupport redisCacheSupport;

    public RedisCacheEvictionHelper(RedisCacheSupport redisCacheSupport) {
        this.redisCacheSupport = redisCacheSupport;
    }

    public void evictPostCollectionCaches() {
        Set<String> postCollectionKeys = redisCacheSupport.scanKeys(IRedisConstant.REDIS_POST.concat("*"))
                .stream()
                .filter(cacheKey -> !cacheKey.startsWith(IRedisConstant.REDIS_POST_ID))
                .collect(Collectors.toSet());

        if (!postCollectionKeys.isEmpty()) {
            redisCacheSupport.delete(postCollectionKeys);
            log.debug("evictPostCollectionCaches - deleted {} post collection cache keys", postCollectionKeys.size());
        }
    }

    public void evictPostIdCaches(Set<Integer> postIdsToDelete) {
        if (postIdsToDelete == null || postIdsToDelete.isEmpty()) {
            return;
        }

        Set<String> cacheKeysToDelete = postIdsToDelete.stream()
                .map(postId -> IRedisConstant.REDIS_POST_ID.concat(String.valueOf(postId)))
                .collect(Collectors.toSet());
        redisCacheSupport.delete(cacheKeysToDelete);
        log.debug("evictPostIdCaches - deleted {} post id cache keys", cacheKeysToDelete.size());
    }

    public void evictCommentCachesForPost(Integer postId) {
        Set<String> commentKeys = redisCacheSupport.scanKeys(IRedisConstant.REDIS_COMMENT_POST.concat(String.valueOf(postId)).concat("*"));
        if (!commentKeys.isEmpty()) {
            redisCacheSupport.delete(commentKeys);
            log.debug("evictCommentCachesForPost - deleted {} comment cache keys for postId={}", commentKeys.size(), postId);
        }
    }
}
