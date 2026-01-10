package com.example.RateLimiter.starter.service;

import org.springframework.stereotype.Service;

import com.example.RateLimiter.starter.algorithm.FixedWindowAlgorithm;
import com.example.RateLimiter.starter.algorithm.RateLimiterAlgorithms;
import com.example.RateLimiter.starter.algorithm.TokenBucketAlgorithm;
import com.example.RateLimiter.starter.model.RateLimiterAlgorithm;
import com.example.RateLimiter.starter.model.RateLimiterProperties;
import com.example.RateLimiter.starter.model.RateLimiterResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimiterService 
{
    private final FixedWindowAlgorithm fixedWindowAlgorithm;
    private final TokenBucketAlgorithm tokenBucketAlgorithm;
    private final RateLimiterProperties properties;


    public RateLimiterResult allowRequest(String key,long limit,long windowSeconds,RateLimiterAlgorithm algorithm)
    {
        if(!properties.isEnabled())
        {
            return createBypassResult(key, limit, algorithm);
        }

        RateLimiterAlgorithms limiter=getAlgorithm(algorithm);
        String fullKey=properties.getKeyPrefix()+key;
        return limiter.allowRequest(fullKey, limit, windowSeconds);

    }

    public RateLimiterResult allowRequestTokenBucket()
    {
        return RateLimiterResult.builder().build();
    }

    public RateLimiterResult getState(String key,long limit,long windowSeconds,RateLimiterAlgorithm algorithm)
    {
        if(!properties.isEnabled())
        {
            return createBypassResult(key, limit, algorithm);
        }
        RateLimiterAlgorithms limiter=getAlgorithm(algorithm);
        String fullKey=properties.getKeyPrefix()+key;
        return limiter.getState(fullKey, limit, windowSeconds);



    }

    public void reset(String key,RateLimiterAlgorithm algorithm)
    {
        RateLimiterAlgorithms limiter=getAlgorithm(algorithm);
        String fullKey=properties.getKeyPrefix()+key;
        limiter.reset(fullKey);

    }

    private RateLimiterAlgorithms getAlgorithm(RateLimiterAlgorithm algorithm)
    {
        return switch(algorithm){
            case FIXED_WINDOW->fixedWindowAlgorithm;
            case TOKEN_BUCKET->tokenBucketAlgorithm;
        };
    }


    private RateLimiterResult createBypassResult(
            String key, 
            long limit, 
            RateLimiterAlgorithm algorithm) {
        return RateLimiterResult.builder()
            .allowed(true)
            .remaining(limit)
            .resetInMs(0)
            .retryAfterMs(0)
            .limit(limit)
            .rateLimiterAlgorithm(algorithm)
            .key(key)
            .build();
    }
}
