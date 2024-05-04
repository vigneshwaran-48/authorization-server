package com.vapps.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vapps.auth.model.UserProvider;

public interface UserProviderRepository extends MongoRepository<UserProvider, String> {
    
}
