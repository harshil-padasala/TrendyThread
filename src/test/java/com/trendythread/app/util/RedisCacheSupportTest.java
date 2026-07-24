package com.trendythread.app.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisCacheSupportTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private RedisConnection redisConnection;

    @Test
    void getDelegatesToValueOperations() {
        RedisCacheSupport support = new RedisCacheSupport(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("key")).thenReturn("value");

        assertEquals("value", support.get("key"));
    }

    @Test
    void setDelegatesToValueOperations() {
        RedisCacheSupport support = new RedisCacheSupport(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        support.set("key", "value", Duration.ofMinutes(5));

        verify(valueOperations).set("key", "value", Duration.ofMinutes(5));
    }

    @Test
    void deleteSingleKeyDelegatesToRedisTemplate() {
        RedisCacheSupport support = new RedisCacheSupport(redisTemplate);

        support.delete("key");

        verify(redisTemplate).delete("key");
    }

    @Test
    void deleteKeyCollectionSkipsEmptyCollections() {
        RedisCacheSupport support = new RedisCacheSupport(redisTemplate);

        support.delete(Set.of());

        verify(redisTemplate, org.mockito.Mockito.never()).delete(any(Set.class));
    }

    @Test
    void scanKeysUsesScanCursorNotBlockingKeysCommand() {
        RedisCacheSupport support = new RedisCacheSupport(redisTemplate);
        Cursor<byte[]> cursor = mockCursor(List.of("REDIS:CATEGORY:ID:1".getBytes(StandardCharsets.UTF_8)));

        when(redisConnection.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<Object> callback = invocation.getArgument(0);
            return callback.doInRedis(redisConnection);
        });

        Set<String> found = support.scanKeys("REDIS:CATEGORY:*");

        assertTrue(found.contains("REDIS:CATEGORY:ID:1"));
        verify(redisTemplate, org.mockito.Mockito.never()).keys(any());
    }

    @SuppressWarnings("unchecked")
    private Cursor<byte[]> mockCursor(List<byte[]> items) {
        Cursor<byte[]> cursor = org.mockito.Mockito.mock(Cursor.class);
        org.mockito.Mockito.doAnswer(invocation -> {
            java.util.function.Consumer<byte[]> consumer = invocation.getArgument(0);
            items.forEach(consumer);
            return null;
        }).when(cursor).forEachRemaining(any());
        return cursor;
    }
}
