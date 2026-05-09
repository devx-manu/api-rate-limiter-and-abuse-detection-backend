package com.demo.API.Rate.Limiter.Abuse.Detection.System.util;

public class TokenBucket {

    private final int capacity;
    private final int refillTokens;
    private final long refillIntervalMillis;

    private int tokens;
    private long lastRefillTime;

    public TokenBucket(int capacity, int refillTokens, long refillIntervalMillis) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillIntervalMillis = refillIntervalMillis;
        this.tokens = capacity;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
        refill();

        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime;

        long intervals = elapsed / refillIntervalMillis;

        if (intervals > 0) {
            int newTokens = (int) (intervals * refillTokens);
            tokens = Math.min(capacity, tokens + newTokens);
            lastRefillTime = now;
        }
    }
}
