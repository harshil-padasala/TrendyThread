package com.trendythread.app.services.impl;

import com.trendythread.app.services.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final String KEY_PREFIX = "REDIS:LOGIN:ATTEMPTS:";

    private final StringRedisTemplate redisTemplate;

    @Value("${security.login-rate-limit.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login-rate-limit.window-minutes:15}")
    private long windowMinutes;

    @Autowired
    public LoginAttemptServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isBlocked(String email) {
        try {
            String value = redisTemplate.opsForValue().get(key(email));
            return value != null && Integer.parseInt(value) >= maxAttempts;
        } catch (DataAccessException ex) {
            log.warn("LoginAttemptService.isBlocked - Redis unavailable, failing open for email={}", email, ex);
            return false;
        }
    }

    @Override
    public void recordFailure(String email) {
        try {
            String k = key(email);
            Long count = redisTemplate.opsForValue().increment(k);
            if (count != null && count == 1L) {
                redisTemplate.expire(k, Duration.ofMinutes(windowMinutes));
            }
            log.debug("LoginAttemptService.recordFailure - email={}, attempt={}", email, count);
        } catch (DataAccessException ex) {
            log.warn("LoginAttemptService.recordFailure - Redis unavailable for email={}", email, ex);
        }
    }

    @Override
    public void recordSuccess(String email) {
        try {
            redisTemplate.delete(key(email));
        } catch (DataAccessException ex) {
            log.warn("LoginAttemptService.recordSuccess - Redis unavailable for email={}", email, ex);
        }
    }

    private String key(String email) {
        return KEY_PREFIX + email.trim().toLowerCase();
    }
}
