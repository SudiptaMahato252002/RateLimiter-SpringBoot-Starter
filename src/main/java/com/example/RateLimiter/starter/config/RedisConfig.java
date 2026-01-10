package com.example.RateLimiter.starter.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.example.RateLimiter.starter.model.RateLimiterProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisConfig 
{
    private final RateLimiterProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public ClientResources clientResources()
    {
        return DefaultClientResources.create();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(ClientResources clientResources)
    {
        RateLimiterProperties.Redis redis=properties.getRedis();

        RedisStandaloneConfiguration redisConfig=new RedisStandaloneConfiguration();
        redisConfig.setHostName(redis.getHost());
        redisConfig.setPort(redis.getPort());
        redisConfig.setDatabase(redis.getDatabase());

        if (redis.getPassword() != null && !redis.getPassword().isEmpty()) {
            redisConfig.setPassword(redis.getPassword());
        }

        ClientOptions clientOptions=ClientOptions.builder()
        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
        .autoReconnect(true)
        .build();

        LettuceClientConfiguration clientConfig=LettucePoolingClientConfiguration.builder()
        .clientResources(clientResources)
        .clientOptions(clientOptions)
        .commandTimeout(Duration.ofMillis(redis.getTimeout()))
        .build();

        return new LettuceConnectionFactory(redisConfig,clientConfig);
    }

    @Bean
    public RedisTemplate<String,Object> rateLimiterRedisTemplate(RedisConnectionFactory redisConnectionFactory)
    {
        RedisTemplate <String,Object> redisTemplate=new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringSerializer=new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);

        ObjectMapper objectMapper=new ObjectMapper();

        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,ObjectMapper.DefaultTyping.NON_FINAL,JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer jsonSerializer=new GenericJackson2JsonRedisSerializer(objectMapper);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        return redisTemplate;
    }
}
