package com.sohan.crudapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HybridCacheManager {

    private final CacheManager caffeine;
    private final CacheManager redis;

    public HybridCacheManager(CaffeineConfig caffeineConfig, RedisConfig redisConfig) {
        this.caffeine = caffeineConfig.caffeineCacheManager(caffeineConfig.caffeineSpec());
        this.redis = redisConfig.cacheManager(redisConfig.redisConnectionFactory());
    }

    public Object get(String cacheName, String key) {
        Cache l1 = caffeine.getCache(cacheName);
        Cache l2 = redis.getCache(cacheName);

        // try caffeine first
        Object value = l1.get(key, Object.class);
        if (value != null) {
            log.info("L1 HIT (Caffeine) key={}", key);
            return value;
        }
        log.warn("L1 MISS key={}", key);

        // try redis
        value = l2.get(key, Object.class);
        if (value != null) {
            log.info("L2 HIT (Redis) key={}", key);
            l1.put(key, value);
            return value;
        }
        log.warn("L2 MISS key={}", key);

        return null;
    }

    public void put(String cacheName, String key, Object value) {
        caffeine.getCache(cacheName).put(key, value);
        redis.getCache(cacheName).put(key, value);
        log.info("CACHE WRITE key={}", key);
    }

    public void evict(String cacheName, String key) {
        caffeine.getCache(cacheName).evict(key);
        redis.getCache(cacheName).evict(key);
        log.info("CACHE EVICT key={}", key);
    }
}
