package com.sohan.crudapp.service;

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

    public UserService(UserRepository repository, UserEventProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @Cacheable(value = "userCache", key = "#id")
    public Optional<User> getUserById(String id) {
//        System.out.println("fetching from db...");
        log.info("Cache Miss for user id={} - loading from db", id);
        return repository.findById(id);
    }

    @CachePut(value = "userCache", key = "#user.id")
    public User createUser(User user) {
        User saved = repository.save(user);
        log.info("User created and cache PUT for id={}", saved.getId());
        // publish event
        UserCreatedEvent event = new UserCreatedEvent(saved.getId(), saved.getName(), saved.getEmail());
        producer.publishUserCreated(event);
        return saved;
    }

    @CacheEvict(value = "userCache", key = "#user.id")
    public User updateUser(String id, User user) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(user.getName());
                    existing.setEmail(user.getEmail());
                    User updated = repository.save(existing);
                    log.info("User updated and cache PUT for id={}", id);
                    return updated;
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @CacheEvict(value = "userCache", key = "#id")
    public void deleteUser(String id) {
        repository.deleteById(id);
        log.info("User deleted and cache EVICT for id={}", id);
    }
}
