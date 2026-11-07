package io.council;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

public class RateLimiter15 {

    public enum Strategy { SLIDING_WINDOW, TOKEN_BUCKET }

    private final int maxRequests;
    private final long windowMillis;
    private final Strategy strategy;

    // Sliding window state
    private final Deque<Long> requestTimestamps;

    // Token bucket state
    private double tokens;
    private long lastRefillTime;
    private final double refillRate;

    public RateLimiter15(int maxRequests, long windowMillis, Strategy strategy) {
        if (maxRequests < 1) throw new IllegalArgumentException("Max requests must be at least 1");
        if (windowMillis < 1) throw new IllegalArgumentException("Window must be positive");
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.strategy = strategy;
        this.requestTimestamps = new ArrayDeque<>();
        this.tokens = maxRequests;
        this.lastRefillTime = Instant.now().toEpochMilli();
        this.refillRate = (double) maxRequests / windowMillis;
    }

    public synchronized boolean allowRequest() {
        return strategy == Strategy.TOKEN_BUCKET ? allowTokenBucket() : allowSlidingWindow();
    }

    private boolean allowSlidingWindow() {
        long now = Instant.now().toEpochMilli();
        long cutoff = now - windowMillis;
        while (!requestTimestamps.isEmpty() && requestTimestamps.peekFirst() <= cutoff) {
            requestTimestamps.pollFirst();
        }
        if (requestTimestamps.size() < maxRequests) {
            requestTimestamps.addLast(now);
            return true;
        }
        return false;
    }

    private boolean allowTokenBucket() {
        long now = Instant.now().toEpochMilli();
        long elapsed = now - lastRefillTime;
        tokens = Math.min(maxRequests, tokens + elapsed * refillRate);
        lastRefillTime = now;
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    public synchronized int getAvailableRequests() {
        if (strategy == Strategy.TOKEN_BUCKET) {
            long now = Instant.now().toEpochMilli();
            long elapsed = now - lastRefillTime;
            double current = Math.min(maxRequests, tokens + elapsed * refillRate);
            return (int) current;
        }
        long now = Instant.now().toEpochMilli();
        long cutoff = now - windowMillis;
        long active = requestTimestamps.stream().filter(t -> t > cutoff).count();
        return (int) Math.max(0, maxRequests - active);
    }

    public synchronized void reset() {
        requestTimestamps.clear();
        tokens = maxRequests;
        lastRefillTime = Instant.now().toEpochMilli();
    }

    public int getMaxRequests() { return maxRequests; }
    public long getWindowMillis() { return windowMillis; }
    public Strategy getStrategy() { return strategy; }
}
