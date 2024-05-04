package com.vapps.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vapps.auth.model.AppUser;

public interface UserRepository extends MongoRepository<AppUser, String> {
    
    AppUser findByUserName(String username);
		
	void deleteByUserName(String username);
	
	Optional<AppUser> findByEmail(String email);
    
}
