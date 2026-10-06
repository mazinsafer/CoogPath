package com.coogpath.coogpath.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import com.coogpath.coogpath.dto.PlanResult;
import com.coogpath.coogpath.service.PlanCache;

/**
 * spring.cache.type picks the backing store (caffeine locally, redis when
 * CACHE_TYPE=redis). This only shapes the Redis variant: plans are stored as
 * JSON so entries stay readable and survive class changes between deploys.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    RedisCacheManagerBuilderCustomizer planCacheRedisConfiguration(
            @Value("${app.cache.plan-ttl:1h}") Duration planTtl) {
        return builder -> builder.withCacheConfiguration(PlanCache.CACHE_NAME,
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(planTtl)
                        .prefixCacheNameWith("coogpath:")
                        .serializeValuesWith(SerializationPair.fromSerializer(
                                new JacksonJsonRedisSerializer<>(PlanResult.class))));
    }
}
