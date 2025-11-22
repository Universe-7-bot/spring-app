package com.sohan.crudapp.service;

import com.sohan.crudapp.event.UserCreatedEvent;
import com.sohan.crudapp.model.User;
import com.sohan.crudapp.producer.UserEventProducer;
import com.sohan.crudapp.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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
        System.out.println("fetching from db...");
        return repository.findById(id);
    }

    public User createUser(User user) {
        User saved = repository.save(user);
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
                    return repository.save(existing);
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @CacheEvict(value = "userCache", key = "#id")
    public void deleteUser(String id) {
        repository.deleteById(id);
    }
}
