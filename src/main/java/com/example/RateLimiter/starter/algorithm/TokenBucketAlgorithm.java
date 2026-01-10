package com.example.RateLimiter.starter.algorithm;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.RateLimiter.starter.model.RateLimiterResult;

import lombok.RequiredArgsConstructor;


@Component("tokenBucketRateLimiter")
@RequiredArgsConstructor
public class TokenBucketAlgorithm implements RateLimiterAlgorithms{

    private final RedisTemplate<String,Object> redisTemplate;
    @Override
    public RateLimiterResult allowRequest(String key, long limit, long windowSeconds) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'allowRequest'");
    }

    @Override
    public RateLimiterResult getState(String key, long limit, long windowSeconds) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getState'");
    }

    @Override
    public void reset(String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reset'");
    }
    
}
