package com.example.RateLimiter.starter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import com.example.RateLimiter.starter.algorithm.FixedWindowAlgorithm;
import com.example.RateLimiter.starter.algorithm.TokenBucketAlgorithm;
import com.example.RateLimiter.starter.aspect.RateLimitAspect;
import com.example.RateLimiter.starter.model.RateLimiterProperties;
import com.example.RateLimiter.starter.service.RateLimiterService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnProperty( prefix="rate-limiter", name="enbaled",havingValue = "true",matchIfMissing = true)
@EnableConfigurationProperties(RateLimiterProperties.class)
@Import({RedisConfig.class})
public class RateLimiterAutoConfiguration 
{
    public RateLimiterAutoConfiguration() {
        log.info("Rate Limiter Spring Boot Starter is enabled");
    }

    @Bean
    public FixedWindowAlgorithm fixedWindowAlgorithm(RedisTemplate<String,Object> redisTemplate)
    {
        log.debug("Initializing Fixed Window Rate Limiter");
        return new FixedWindowAlgorithm(redisTemplate);
    }

    @Bean
    public TokenBucketAlgorithm tokenBucketAlgorithm(RedisTemplate<String,Object> redisTemplate)
    {
        log.debug("Initializing Token Bucket Rate Limiter");
        return new TokenBucketAlgorithm(redisTemplate);
    }

    @Bean
    public RateLimiterService rateLimiterService(FixedWindowAlgorithm fixedWindowAlgorithm,TokenBucketAlgorithm tokenBucketAlgorithm,RateLimiterProperties properties)
    {
         log.debug("Initializing Rate Limiter Service");
        return new RateLimiterService(fixedWindowAlgorithm, tokenBucketAlgorithm, properties);
    }

    @Bean
    public RateLimitAspect rateLimitAspect(RateLimiterService rateLimiterService) {
        log.debug("Initializing Rate Limit Aspect");
        return new RateLimitAspect(rateLimiterService);
    }

}
