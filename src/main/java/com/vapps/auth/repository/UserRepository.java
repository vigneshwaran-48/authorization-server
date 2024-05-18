package com.vapps.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vapps.auth.model.AppUser;
import com.vapps.auth.util.OAuth2Provider;

public interface UserRepository extends MongoRepository<AppUser, String> {
    
    AppUser findByUserName(String username);

    Optional<AppUser> findByUserNameAndProvider(String username, OAuth2Provider provider);
		
	void deleteByUserName(String username);
	
	Optional<AppUser> findByEmail(String email);

	Optional<AppUser> findByEmailAndProvider(String email, OAuth2Provider provider);
    
}
