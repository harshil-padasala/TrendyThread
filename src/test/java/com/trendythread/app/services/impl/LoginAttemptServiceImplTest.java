package com.trendythread.app.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private LoginAttemptServiceImpl service;

    private static final String EMAIL = "reader@example.com";
    private static final String KEY = "REDIS:LOGIN:ATTEMPTS:reader@example.com";

    @BeforeEach
    void setUp() {
        service = new LoginAttemptServiceImpl(redisTemplate);
        ReflectionTestUtils.setField(service, "maxAttempts", 5);
        ReflectionTestUtils.setField(service, "windowMinutes", 15L);
    }

    @Test
    void isBlockedReturnsFalseWhenUnderThreshold() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn("3");

        assertFalse(service.isBlocked(EMAIL));
    }

    @Test
    void isBlockedReturnsTrueAtThreshold() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn("5");

        assertTrue(service.isBlocked(EMAIL));
    }

    @Test
    void isBlockedReturnsFalseWhenNoAttemptsRecorded() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn(null);

        assertFalse(service.isBlocked(EMAIL));
    }

    @Test
    void isBlockedFailsOpenWhenRedisUnavailable() {
        when(redisTemplate.opsForValue()).thenThrow(new QueryTimeoutException("redis down"));

        assertFalse(service.isBlocked(EMAIL));
    }

    @Test
    void recordFailureSetsExpiryOnlyOnFirstAttempt() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(KEY)).thenReturn(1L);

        service.recordFailure(EMAIL);

        verify(redisTemplate).expire(eq(KEY), eq(Duration.ofMinutes(15)));
    }

    @Test
    void recordFailureDoesNotResetExpiryOnSubsequentAttempts() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(KEY)).thenReturn(2L);

        service.recordFailure(EMAIL);

        verify(redisTemplate, never()).expire(any(), any());
    }

    @Test
    void recordSuccessDeletesTheCounterKey() {
        service.recordSuccess(EMAIL);

        verify(redisTemplate).delete(KEY);
    }

    @Test
    void emailMatchingIsCaseInsensitiveAndTrimmed() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(KEY)).thenReturn("5");

        assertTrue(service.isBlocked("  Reader@Example.com  "));
    }
}
