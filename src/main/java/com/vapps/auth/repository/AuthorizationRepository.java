package com.vapps.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vapps.auth.model.Authorization;

public interface AuthorizationRepository extends MongoRepository<Authorization, String> {
    
    Optional<Authorization> findByState(String state);
	
	Optional<Authorization> findByAuthorizationCodeValue(String authorizationCodeValue);
	
	Optional<Authorization> findByAccessTokenValue(String accessTokenValue);
	
	Optional<Authorization> findByRefreshTokenValue(String refreshTokenValue);
	
	Optional<Authorization> findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValue(String token);
}
