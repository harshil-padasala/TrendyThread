package com.trendythread.app.services;

import org.springframework.stereotype.Service;

@Service
public interface LoginAttemptService {

    /**
     * Whether this email has hit the failed-login threshold and should be
     * blocked from attempting to authenticate right now.
     */
    boolean isBlocked(String email);

    /** Records a failed login attempt, starting/continuing the rate-limit window. */
    void recordFailure(String email);

    /** Clears the failure count for this email (called on successful login). */
    void recordSuccess(String email);
}
