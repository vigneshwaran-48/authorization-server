package com.vapps.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.transaction.annotation.Transactional;

import com.vapps.auth.model.Scope;

public interface ScopeRepository extends MongoRepository<Scope, String> {
    
    Optional<Scope> findByScopeName(String scopeName);
    Optional<Scope> findByClientIdAndScopeName(String clientId, String scopeName);
    @Transactional
    List<Scope> deleteByClientClientId(String clientId);
    
}
