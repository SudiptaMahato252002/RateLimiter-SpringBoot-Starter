package com.example.RateLimiter.starter.algorithm;

import com.example.RateLimiter.starter.model.RateLimiterResult;

public class TokenBucketAlgorithm implements RateLimiterAlgorithms{

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
