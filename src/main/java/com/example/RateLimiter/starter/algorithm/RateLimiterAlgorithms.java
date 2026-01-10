package com.example.RateLimiter.starter.algorithm;

import com.example.RateLimiter.starter.model.RateLimiterResult;

public interface RateLimiterAlgorithms 
{
    RateLimiterResult allowRequest(String key,long limit,long windowSeconds);

    RateLimiterResult getState(String key,long limit,long windowSeconds);

    void reset(String key);
    
}
