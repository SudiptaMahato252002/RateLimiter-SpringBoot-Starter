package com.example.RateLimiter.starter.algorithm;

import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.example.RateLimiter.starter.model.RateLimiterAlgorithm;
import com.example.RateLimiter.starter.model.RateLimiterResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("fixedWindowRateLimiter")
@RequiredArgsConstructor
public class FixedWindowAlgorithm implements RateLimiterAlgorithms
{
    private final RedisTemplate<String,Object> redisTemplate;

    private static final String LUA_SCRIPT=
    "local current=redis.call('incr',KEYS[1]) "+
    "if current==1 then "+
    "   redis.call('expire',KEYS[1],ARGV[1]) "+
    "end "+
    "local ttl=redis.call('ttl',KEYS[1]) "+
    "return {current,ttl}";

    @Override
    public RateLimiterResult allowRequest(String key,long limit,long windowSeconds)
    {
        try 
        {
            DefaultRedisScript<List> scripts=new DefaultRedisScript<>();
            scripts.setScriptText(LUA_SCRIPT);
            scripts.setResultType(List.class);


            List<Long> result=(List<Long>)redisTemplate.execute(scripts,Collections.singletonList(key),windowSeconds);

            if (result == null || result.size() != 2) {
                    log.error("Unexpected result from Redis script for key: {}", key);
                    return createErrorResult(key, limit, windowSeconds);
                }

            long current=result.get(0);
            long ttl=result.get(1);



            boolean allowed=current<=limit;
            long remaining=Math.max(0,limit-current);
            long retryAfterMs=allowed?0:ttl*1000;

            return RateLimiterResult.builder()
                .allowed(allowed)
                .remaining(remaining)
                .resetInMs(ttl*1000)
                .retryAfterMs(retryAfterMs)
                .rateLimiterAlgorithm(RateLimiterAlgorithm.FIXED_WINDOW)
                .limit(limit)
                .key(key)
            .build();
                
        } 
        catch (Exception e) 
        {
            log.error("Error executing fixed window rate limiter for key: {}", key, e);
            return createErrorResult(key, limit, windowSeconds);
        }

        

    }

    @Override
    public RateLimiterResult getState(String key,long limit,long windowSeconds)
    {   
        try 
        {
            String currentStr=(String)redisTemplate.opsForValue().get(key);
            long current=currentStr!=null?Long.parseLong(currentStr):0;
            Long ttl=redisTemplate.getExpire(key);


            if (ttl == null || ttl < 0) {
                ttl = windowSeconds;
            }
            boolean allowed = current < limit;
            long remaining = Math.max(0, limit - current);
            return RateLimiterResult.builder()
                .allowed(allowed)
                .remaining(remaining)
                .resetInMs(windowSeconds)
                .retryAfterMs(0)
                .limit(limit)
                .rateLimiterAlgorithm(RateLimiterAlgorithm.FIXED_WINDOW)
                .key(key)
            .build();
        } 
        catch (Exception e) 
        {
            log.error("Error getting state for key: {}", key, e);
            return createErrorResult(key, limit, windowSeconds);
        }

    }

    @Override
    public void reset(String key)
    {
        try {
            redisTemplate.delete(key);
            log.debug("Reset rate limit for key: {}", key);

        } 
        catch (Exception e) 
        {
            log.error("Error resetting rate limit for key: {}", key, e);
        }
    }

    private RateLimiterResult createErrorResult(String key,long limit,long windowSeconds)
    {
        return RateLimiterResult.builder()
            .allowed(true)
            .remaining(limit)
            .resetInMs(windowSeconds)
            .retryAfterMs(0)
            .rateLimiterAlgorithm(RateLimiterAlgorithm.FIXED_WINDOW)
            .limit(limit)
            .key(key)
        .build();
    }
}
