package com.trendythread.app.config.logging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * ExecutorConfig
 * <p>
 * Purpose:
 * - Provides a configurable ThreadPoolTaskExecutor bean that uses MdcTaskDecorator to
 *   propagate MDC context (e.g., requestId) from the submitting thread to the worker thread.
 * <p>
 * Disabled by default:
 * - This configuration is annotated with @ConditionalOnProperty(prefix = "app.executor",
 *   name = "enabled", havingValue = "true", matchIfMissing = false). That means the bean
 *   will NOT be created unless you explicitly enable it in application.yml or environment
 *   properties by setting:
 * <p>
 *     app:
 *       executor:
 *       enabled: true
 *       corePoolSize: 5
 *       maxPoolSize: 20
 *       queueCapacity: 50
 *       threadNamePrefix: app-exec-
 * <p>
 * How to use once enabled:
 * 1. Enable the executor by setting the property above.
 * 2. Option A - Use @Async on methods that should run asynchronously and annotate them with
 *    @Async("appTaskExecutor"). Make sure you have @EnableAsync on a configuration class.
 * 3. Option B - Inject the Executor (or ThreadPoolTaskExecutor) bean named "appTaskExecutor"
 *    and call execute()/submit() as needed. MDC context will be propagated to the worker thread.
 * <p>
 * Configuration properties:
 * - corePoolSize, maxPoolSize, queueCapacity and threadNamePrefix are configurable via
 *   constructor arguments using @Value placeholders. They have sane defaults but can be tuned
 *   per environment by adding properties under app.executor in application.yml.
 * <p>
 * Why keep disabled by default?
 * - Not all apps require MDC propagation to async tasks. Keeping this opt-in avoids altering
 *   threadpool behavior unexpectedly. Enable it when you need consistent request-scoped logging
 *   across asynchronous work.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.executor", name = "enabled", havingValue = "true", matchIfMissing = false)
public class ExecutorConfig {

    private final MdcTaskDecorator mdcTaskDecorator;

    public ExecutorConfig(MdcTaskDecorator mdcTaskDecorator) {
        this.mdcTaskDecorator = mdcTaskDecorator;
    }

    @Bean(name = "appTaskExecutor")
    public Executor appTaskExecutor(
            @Value("${app.executor.corePoolSize:5}") int corePoolSize,
            @Value("${app.executor.maxPoolSize:20}") int maxPoolSize,
            @Value("${app.executor.queueCapacity:50}") int queueCapacity,
            @Value("${app.executor.threadNamePrefix:app-exec-}") String threadNamePrefix
    ) {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(corePoolSize);
        exec.setMaxPoolSize(maxPoolSize);
        exec.setQueueCapacity(queueCapacity);
        exec.setThreadNamePrefix(threadNamePrefix);

        // Attach MDC-propagating TaskDecorator
        exec.setTaskDecorator(mdcTaskDecorator);

        exec.initialize();
        return exec;
    }
}

