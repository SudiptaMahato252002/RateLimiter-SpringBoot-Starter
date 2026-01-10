package com.example.RateLimiter.starter.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "rate-limiter:")
public class RateLimiterProperties 
{
    private boolean enabled=true;

    private Redis redis=new Redis();

    private Defaults defaults=new Defaults();

    private boolean includeHeaders=true;

    private String keyPrefix="rate-limit:";


    @Data
    public static class Redis
    {
        private String host;
        private int port;
        private String password;
        private int database=0;
        private int maxConnections=0;
        private int timeout=2000;


    }

    @Data
    public static class Defaults
    {

        private long window=100;

        private long limit=60;
        
        private double refillRate=10.0;
    }
}

