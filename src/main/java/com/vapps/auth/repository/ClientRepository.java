package com.vapps.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vapps.auth.model.Client;

public interface ClientRepository extends MongoRepository<Client, String> {
	
	Optional<Client> findByClientId(String clientId);
	
	void deleteByClientId(String clientId);
}
