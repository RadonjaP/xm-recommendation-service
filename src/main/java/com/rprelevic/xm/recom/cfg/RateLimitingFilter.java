package com.rprelevic.xm.recom.cfg;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Duration;

/**
 * Filter that implements rate limiting using Bucket4j.
 */
public class RateLimitingFilter implements Filter {

    private final Bucket bucket;

    /**
     * Constructs a RateLimitingFilter with a limit of 20 requests per minute.
     * Hardcoded for simplicity, but it can be made configurable through constructor parameters.
     */
    public RateLimitingFilter() {
        Bandwidth limit = Bandwidth.simple(20, Duration.ofMinutes(1));
        this.bucket = Bucket.builder().addLimit(limit).build();
    }

    /**
     * Filters incoming requests and applies rate limiting.
     *
     * @param request the ServletRequest object
     * @param response the ServletResponse object
     * @param chain the FilterChain object
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setStatus(429);
            response.getWriter().write("Rate limit exceeded. Try again later.");
        }
    }
}