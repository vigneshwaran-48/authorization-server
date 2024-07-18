package com.vapps.auth.service;

import java.util.List;
import java.util.Optional;

import com.vapps.auth.dto.ClientDTO;
import com.vapps.auth.dto.ScopeDTO;
import com.vapps.auth.exception.AppException;

public interface ScopeService {

    Optional<ScopeDTO> getScopesOfClient();

    Optional<ScopeDTO> getScope(String clientId, Long scopeId);

    Optional<ScopeDTO> getScope(String scopeName);

    String createScope(ClientDTO client, ScopeDTO scopeDetails) throws AppException;

    String deleteScope(String clientId, Long scopeId);

    boolean isScopePresent(String clientId, Long scopeId);

    List<String> deleteAllScopesOfClient(String userId, String clientId);

    List<String> checkAndScopes(ClientDTO client, List<ScopeDTO> scopes) throws AppException;
}
