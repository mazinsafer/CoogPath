package com.coogpath.coogpath.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

/**
 * Token-bucket limiter keyed by an arbitrary string (here, client IP). Each key
 * holds up to {@code requestsPerMinute} tokens and regains them continuously.
 * State is in memory, so with several API instances each enforces its own limit.
 */
public final class RateLimiter {

    private static final long NANOS_PER_MINUTE = TimeUnit.MINUTES.toNanos(1);
    private static final int SWEEP_EVERY = 1024;

    private final int capacity;
    private final double tokensPerNano;
    private final LongSupplier nanoClock;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AtomicInteger callsSinceSweep = new AtomicInteger();

    public RateLimiter(int requestsPerMinute) {
        this(requestsPerMinute, System::nanoTime);
    }

    RateLimiter(int requestsPerMinute, LongSupplier nanoClock) {
        if (requestsPerMinute <= 0) throw new IllegalArgumentException("requestsPerMinute must be positive");
        this.capacity = requestsPerMinute;
        this.tokensPerNano = (double) requestsPerMinute / NANOS_PER_MINUTE;
        this.nanoClock = nanoClock;
    }

    /** Takes one token for {@code key}. Returns 0 if allowed, otherwise the seconds until a token is available. */
    public long tryAcquire(String key) {
        long now = nanoClock.getAsLong();
        if (callsSinceSweep.incrementAndGet() >= SWEEP_EVERY) {
            callsSinceSweep.set(0);
            sweepFullBuckets(now);
        }
        return buckets.computeIfAbsent(key, k -> new Bucket(capacity, now)).tryTake(now);
    }

    int trackedKeys() {
        return buckets.size();
    }

    /** A full bucket behaves exactly like a missing one, so it can be dropped to bound memory. */
    private void sweepFullBuckets(long now) {
        buckets.values().removeIf(bucket -> bucket.isFull(now));
    }

    private final class Bucket {
        private double tokens;
        private long lastRefill;

        Bucket(double tokens, long now) {
            this.tokens = tokens;
            this.lastRefill = now;
        }

        synchronized long tryTake(long now) {
            refill(now);
            if (tokens >= 1) {
                tokens -= 1;
                return 0;
            }
            double nanosUntilToken = (1 - tokens) / tokensPerNano;
            return Math.max(1, (long) Math.ceil(nanosUntilToken / TimeUnit.SECONDS.toNanos(1)));
        }

        synchronized boolean isFull(long now) {
            refill(now);
            return tokens >= capacity;
        }

        private void refill(long now) {
            tokens = Math.min(capacity, tokens + (now - lastRefill) * tokensPerNano);
            lastRefill = now;
        }
    }
}
