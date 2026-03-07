package com.trendythread.app.config.logging;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MdcTaskDecorator
 *
 * Purpose:
 * - Captures the SLF4J MDC (Mapped Diagnostic Context) from the thread that submits a task
 *   and copies it to the worker thread that executes the Runnable. This allows MDC keys
 *   like "requestId" to be available in logs produced by async tasks executed by a
 *   ThreadPoolTaskExecutor.
 *
 * When to use:
 * - Use this decorator when your application uses MDC (for example, a RequestId put into MDC
 *   by a servlet filter) and you also submit work to a thread pool (e.g., via @Async or
 *   manually submitting Runnables). Without this decorator, MDC is thread-local and will not
 *   be visible in the worker thread.
 *
 * How to use:
 * 1. Register this class as the TaskDecorator on your ThreadPoolTaskExecutor (see ExecutorConfig).
 * 2. Ensure the executor bean is used where you need MDC propagation, for example:
 *    - Annotate a method with @Async("appTaskExecutor") where you want MDC to be preserved.
 *    - Or inject the ThreadPoolTaskExecutor bean and use it to submit tasks.
 *
 * Important notes:
 * - The decorator copies the MDC map at submission time. Subsequent changes to the submitting
 *   thread's MDC will not affect already-submitted tasks.
 * - It restores the previous MDC state of the worker thread after task execution to avoid
 *   leaking MDC values between tasks.
 */
@Component
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // Capture the current MDC context from the submitting thread
        final Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            // Preserve the worker thread's previous MDC to restore after execution
            final Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                } else {
                    MDC.clear();
                }
                runnable.run();
            } finally {
                // Restore previous MDC (could be null)
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        };
    }
}

