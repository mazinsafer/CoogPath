package com.coogpath.coogpath.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

class RateLimiterTest {

    private final AtomicLong now = new AtomicLong();

    private void advanceSeconds(long seconds) {
        now.addAndGet(TimeUnit.SECONDS.toNanos(seconds));
    }

    @Test
    void allows_a_full_minute_of_requests_then_blocks() {
        RateLimiter limiter = new RateLimiter(3, now::get);

        assertEquals(0, limiter.tryAcquire("1.2.3.4"));
        assertEquals(0, limiter.tryAcquire("1.2.3.4"));
        assertEquals(0, limiter.tryAcquire("1.2.3.4"));
        assertTrue(limiter.tryAcquire("1.2.3.4") > 0);
    }

    @Test
    void reports_seconds_until_the_next_token() {
        RateLimiter limiter = new RateLimiter(6, now::get);
        for (int i = 0; i < 6; i++) limiter.tryAcquire("client");

        // 6 per minute refills one token every 10 seconds.
        assertEquals(10, limiter.tryAcquire("client"));
        advanceSeconds(4);
        assertEquals(6, limiter.tryAcquire("client"));
    }

    @Test
    void tokens_refill_over_time() {
        RateLimiter limiter = new RateLimiter(2, now::get);
        limiter.tryAcquire("client");
        limiter.tryAcquire("client");
        assertTrue(limiter.tryAcquire("client") > 0);

        advanceSeconds(30);
        assertEquals(0, limiter.tryAcquire("client"));
    }

    @Test
    void clients_are_limited_independently() {
        RateLimiter limiter = new RateLimiter(1, now::get);

        assertEquals(0, limiter.tryAcquire("a"));
        assertTrue(limiter.tryAcquire("a") > 0);
        assertEquals(0, limiter.tryAcquire("b"));
    }

    @Test
    void idle_clients_are_forgotten() {
        RateLimiter limiter = new RateLimiter(1, now::get);
        for (int i = 0; i < 2000; i++) limiter.tryAcquire("client-" + i);

        advanceSeconds(120);
        for (int i = 0; i < 1024; i++) limiter.tryAcquire("recent");

        assertEquals(1, limiter.trackedKeys());
    }
}
