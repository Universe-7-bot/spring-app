package com.sohan.crudapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/admin/cache")
@RequiredArgsConstructor
public class CacheInspectorController {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USERS_CACHE_PREFIX = "userCache::";

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> listUserCacheKeys() {
        Set<String> keys = redisTemplate.keys(USERS_CACHE_PREFIX + "*");

        Map<String, Object> response = new HashMap<>();
        response.put("count", keys.size());
        response.put("keys", keys);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserCacheEntry(@PathVariable String id) {
        String redisKey = USERS_CACHE_PREFIX + id;
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Object value = ops.get(redisKey);

        Map<String, Object> response = new HashMap<>();
        response.put("redisKey", redisKey);
        response.put("exists", value != null);
        response.put("value", value);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUserCacheEntry(@PathVariable String id) {
        String redisKey = USERS_CACHE_PREFIX + id;
        Boolean removed = redisTemplate.delete(redisKey);

        Map<String, Object> response = new HashMap<>();
        response.put("redisKey", redisKey);
        response.put("deleted", Boolean.TRUE.equals(removed));

        return ResponseEntity.ok(response);
    }
}
