package com.trendythread.app.config.logging;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String reqId = request.getHeader(REQUEST_ID_HEADER);
        if (reqId == null || reqId.isBlank()) {
            reqId = UUID.randomUUID().toString();
        }

        // Put into MDC so logback %X{requestId} will print it
        MDC.put(MDC_REQUEST_ID_KEY, reqId);

        // Also set header on response for client visibility
        response.setHeader(REQUEST_ID_HEADER, reqId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Important: remove MDC entries to avoid leaking into other requests/threads
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }
}

