package com.sohan.crudapp.service;

import com.sohan.crudapp.config.HybridCacheManager;
import com.sohan.crudapp.event.UserCreatedEvent;
import com.sohan.crudapp.model.User;
import com.sohan.crudapp.producer.UserEventProducer;
import com.sohan.crudapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository repository;
    private final UserEventProducer producer;
    private final HybridCacheManager cache;

    private static final String CACHE_NAME = "users";

    public UserService(UserRepository repository, UserEventProducer producer, HybridCacheManager cache) {
        this.repository = repository;
        this.producer = producer;
        this.cache = cache;
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

//    @Cacheable(value = "userCache", key = "#id")
    public Optional<User> getUserById(String id) {
//        System.out.println("fetching from db...");
//        log.info("Cache Miss for user id={} - loading from db", id);

        // try hybrid cache
        User cached = (User) cache.get(CACHE_NAME, id);
        if (cached != null) return Optional.of(cached);

        // DB fallback
        User dbUser = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        cache.put(CACHE_NAME, id, dbUser);

        log.info("CACHE FILLED key={}", id);
        return Optional.of(dbUser);

//        return repository.findById(id);
    }

//    @CachePut(value = "userCache", key = "#user.id")
    public User createUser(User user) {
        User saved = repository.save(user);
//        log.info("User created and cache PUT for id={}", saved.getId());

        // hybrid cache
        cache.put(CACHE_NAME, saved.getId(), saved);

        // publish event
        UserCreatedEvent event = new UserCreatedEvent(saved.getId(), saved.getName(), saved.getEmail());
        producer.publishUserCreated(event);

        return saved;
    }

//    @CacheEvict(value = "userCache", key = "#user.id")
    public User updateUser(String id, User user) {
        User updated = repository.findById(id)
                .map(existing -> {
                    existing.setName(user.getName());
                    existing.setEmail(user.getEmail());

                    return repository.save(existing);

//                    User updated = repository.save(existing);
//                    log.info("User updated and cache PUT for id={}", id);
//                    return updated;
                }).orElseThrow(() -> new RuntimeException("User not found"));

        cache.put(CACHE_NAME, updated.getId(), updated);

        return updated;
    }

//    @CacheEvict(value = "userCache", key = "#id")
    public void deleteUser(String id) {
        repository.deleteById(id);
//        log.info("User deleted and cache EVICT for id={}", id);

        cache.evict(CACHE_NAME, id);
    }
}
