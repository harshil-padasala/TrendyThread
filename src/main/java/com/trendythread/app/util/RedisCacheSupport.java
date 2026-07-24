package com.trendythread.app.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class RedisCacheSupport {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheSupport(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** Redis failures are treated as cache misses so callers fall through to the DB. */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (DataAccessException ex) {
            log.warn("RedisCacheSupport.get - Redis unavailable for key={}, treating as cache miss", key, ex);
            return null;
        }
    }

    /** Cache writes are best-effort (already deferred to after-commit) - failures are logged and swallowed. */
    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (DataAccessException ex) {
            log.warn("RedisCacheSupport.set - failed to write key={}", key, ex);
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException ex) {
            log.warn("RedisCacheSupport.delete - failed to delete key={}", key, ex);
        }
    }

    public void delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        try {
            redisTemplate.delete(keys);
        } catch (DataAccessException ex) {
            log.warn("RedisCacheSupport.delete - failed to delete {} keys", keys.size(), ex);
        }
    }

    /** Non-blocking replacement for RedisTemplate#keys(pattern), backed by SCAN instead of the blocking KEYS command. */
    public Set<String> scanKeys(String matchPattern) {
        Set<String> found = new HashSet<>();
        try {
            redisTemplate.execute((RedisCallback<Object>) connection -> {
                try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(matchPattern).count(500).build())) {
                    cursor.forEachRemaining(key -> found.add(new String(key, StandardCharsets.UTF_8)));
                }
                return null;
            });
        } catch (DataAccessException ex) {
            log.warn("RedisCacheSupport.scanKeys - failed to scan pattern={}, returning {} keys found so far", matchPattern, found.size(), ex);
        }
        return found;
    }
}
