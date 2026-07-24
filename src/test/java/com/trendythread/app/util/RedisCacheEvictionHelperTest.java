package com.trendythread.app.util;

import com.trendythread.app.constants.IRedisConstant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisCacheEvictionHelperTest {

    @Mock
    private RedisCacheSupport redisCacheSupport;

    @Test
    void evictPostCollectionCachesFiltersOutPostIdKeysBeforeDeleting() {
        RedisCacheEvictionHelper helper = new RedisCacheEvictionHelper(redisCacheSupport);
        Set<String> scannedKeys = Set.of(
                IRedisConstant.REDIS_POST_ALL.concat(":page:0:size:10"),
                IRedisConstant.REDIS_POST_ID.concat("5")
        );
        when(redisCacheSupport.scanKeys(IRedisConstant.REDIS_POST.concat("*"))).thenReturn(scannedKeys);

        helper.evictPostCollectionCaches();

        verify(redisCacheSupport).delete(argThat((Set<String> keys) ->
                keys.equals(Set.of(IRedisConstant.REDIS_POST_ALL.concat(":page:0:size:10")))));
    }

    @Test
    void evictPostCollectionCachesSkipsDeleteWhenNothingMatches() {
        RedisCacheEvictionHelper helper = new RedisCacheEvictionHelper(redisCacheSupport);
        when(redisCacheSupport.scanKeys(IRedisConstant.REDIS_POST.concat("*")))
                .thenReturn(Set.of(IRedisConstant.REDIS_POST_ID.concat("5")));

        helper.evictPostCollectionCaches();

        verify(redisCacheSupport, never()).delete((Set<String>) org.mockito.ArgumentMatchers.any());
    }

    @Test
    void evictPostIdCachesSkipsEmptyOrNullSets() {
        RedisCacheEvictionHelper helper = new RedisCacheEvictionHelper(redisCacheSupport);

        helper.evictPostIdCaches(null);
        helper.evictPostIdCaches(Set.of());

        verify(redisCacheSupport, never()).delete((Set<String>) org.mockito.ArgumentMatchers.any());
    }

    @Test
    void evictPostIdCachesDeletesMappedKeys() {
        RedisCacheEvictionHelper helper = new RedisCacheEvictionHelper(redisCacheSupport);

        helper.evictPostIdCaches(Set.of(7));

        verify(redisCacheSupport).delete(Set.of(IRedisConstant.REDIS_POST_ID.concat("7")));
    }

    @Test
    void evictCommentCachesForPostDeletesScannedKeys() {
        RedisCacheEvictionHelper helper = new RedisCacheEvictionHelper(redisCacheSupport);
        Set<String> commentKeys = Set.of(IRedisConstant.REDIS_COMMENT_POST.concat("9:LIST"));
        when(redisCacheSupport.scanKeys(IRedisConstant.REDIS_COMMENT_POST.concat("9").concat("*"))).thenReturn(commentKeys);

        helper.evictCommentCachesForPost(9);

        verify(redisCacheSupport).delete(commentKeys);
    }
}
