package com.example.RateLimiter.starter.exception;

import com.example.RateLimiter.starter.model.RateLimiterResult;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException
{
    private RateLimiterResult rateLimiterResult;

    public RateLimitExceededException(String message,RateLimiterResult result)
    {
        super(message);
        this.rateLimiterResult=result;
    }

    public RateLimitExceededException(RateLimiterResult result)
    {
        super(String.format("RateLimit exceeded.Retry after %d ms",result.getResetInMs()));
        this.rateLimiterResult=result;
    }
    
}
