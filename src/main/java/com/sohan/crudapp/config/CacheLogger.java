package com.sohan.crudapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheLogger {

    public void hit(String key, String layer) {
        log.info("CACHE HIT [{}] key={}", layer, key);
    }

    public void miss(String key, String layer) {
        log.info("CACHE MISS [{}] key={}", layer, key);
    }
}
