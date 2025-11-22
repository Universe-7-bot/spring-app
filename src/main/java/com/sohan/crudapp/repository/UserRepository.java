package com.sohan.crudapp.repository;

import com.sohan.crudapp.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}