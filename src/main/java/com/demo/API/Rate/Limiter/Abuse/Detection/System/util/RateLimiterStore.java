package com.demo.API.Rate.Limiter.Abuse.Detection.System.util;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class RateLimiterStore {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public TokenBucket getBucket(String key) {
        return buckets.computeIfAbsent(key, k ->
                new TokenBucket(10, 2, 1000)); // 10 capacity, 2 tokens/sec
    }
}
