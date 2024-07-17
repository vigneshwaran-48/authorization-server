package com.vapps.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vapps.auth.model.Client;

public interface ClientRepository extends MongoRepository<Client, String> {
	
	Optional<Client> findByClientId(String clientId);
	Optional<Client> findByClientIdAndUserId(String clientId, String userId);
	void deleteByClientId(String clientId);
	List<Client> findByUserId(String userId);
	Optional<Client> findByClientNameIgnoreCase(String clientName);
}
