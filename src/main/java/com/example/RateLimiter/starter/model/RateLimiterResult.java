package com.example.RateLimiter.starter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimiterResult 
{
    private boolean allowed;

    private long remaining;

    private long resetInMs;

    private long retryAfterMs;

    private RateLimiterAlgorithm rateLimiterAlgorithm;

    private long limit;

    private String key;
    
}
