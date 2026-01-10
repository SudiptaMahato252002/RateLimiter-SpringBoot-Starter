package com.example.RateLimiter.starter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.RateLimiter.starter.model.RateLimiterAlgorithm;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit 
{
    String key();

    long limit() default 100;

    long windowSeconds() default 60;

    double refillRate() default 10.0;
    
    RateLimiterAlgorithm algorithm() default RateLimiterAlgorithm.FIXED_WINDOW;

    boolean throwException() default true;
    
    String errorMessage() default "Rate limit exceeded";

    String keyParam() default "";
}
