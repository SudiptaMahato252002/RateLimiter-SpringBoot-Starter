package com.example.RateLimiter.starter.model;

import lombok.Data;

public class RateLimiterProperties 
{



    @Data
    public static class Redis
    {
        private String host;
        private int port;
        private String password;

    }

    @Data
    public static class Defaults
    {

        private long window=100;

        private long limit=60;
        
        private double refillRate;
    }
}

