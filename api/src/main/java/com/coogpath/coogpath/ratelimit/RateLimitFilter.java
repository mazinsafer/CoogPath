package com.coogpath.coogpath.ratelimit;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Per-client request limits for /api/**, with a tighter budget for sign-in and
 * sign-up (password guessing) and plan generation (the most expensive call).
 * Runs after Spring Security, so 429 responses still carry CORS headers.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    static final String TOO_MANY_REQUESTS = "Too many requests. Please wait a moment and try again.";

    private final boolean enabled;
    private final boolean trustForwardedFor;
    private final RateLimiter authLimiter;
    private final RateLimiter planLimiter;
    private final RateLimiter advisorLimiter;
    private final RateLimiter defaultLimiter;

    public RateLimitFilter(
            @Value("${app.rate-limit.enabled:true}") boolean enabled,
            @Value("${app.rate-limit.trust-forwarded-for:false}") boolean trustForwardedFor,
            @Value("${app.rate-limit.auth-per-minute:10}") int authPerMinute,
            @Value("${app.rate-limit.plan-per-minute:30}") int planPerMinute,
            @Value("${app.rate-limit.advisor-per-minute:10}") int advisorPerMinute,
            @Value("${app.rate-limit.default-per-minute:300}") int defaultPerMinute) {
        this.enabled = enabled;
        this.trustForwardedFor = trustForwardedFor;
        this.authLimiter = new RateLimiter(authPerMinute);
        this.planLimiter = new RateLimiter(planPerMinute);
        this.advisorLimiter = new RateLimiter(advisorPerMinute);
        this.defaultLimiter = new RateLimiter(defaultPerMinute);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled
                || HttpMethod.OPTIONS.matches(request.getMethod())
                || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long retryAfterSeconds = limiterFor(request).tryAcquire(clientIp(request));
        if (retryAfterSeconds > 0) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"" + TOO_MANY_REQUESTS + "\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private RateLimiter limiterFor(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean isPost = HttpMethod.POST.matches(request.getMethod());
        if (isPost && (path.equals("/api/auth/login") || path.equals("/api/students/register"))) {
            return authLimiter;
        }
        if (path.startsWith("/api/plan/generate/")) {
            return planLimiter;
        }
        if (isPost && path.startsWith("/api/advisor/")) {
            return advisorLimiter;
        }
        return defaultLimiter;
    }

    /**
     * Behind a proxy (Railway, Render, etc.) the socket address is the proxy's.
     * The proxy appends the real client to X-Forwarded-For, so the right-most
     * entry is the only one a client can't forge.
     */
    private String clientIp(HttpServletRequest request) {
        if (trustForwardedFor) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                String[] hops = forwarded.split(",");
                return hops[hops.length - 1].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
