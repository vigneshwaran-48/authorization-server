package com.vapps.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vapps.auth.model.AuthorizationConsent;

public interface AuthorizationConsentRepository extends MongoRepository<AuthorizationConsent, String> {

    Optional<AuthorizationConsent> findByRegisteredClientIdAndPrincipalName(String registeredClientId,
            String principalName);

    void deleteByRegisteredClientIdAndPrincipalName(String registeredClientId, String principalName);

}
