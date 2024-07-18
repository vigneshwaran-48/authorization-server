package com.vapps.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vapps.auth.dto.ClientDTO;
import com.vapps.auth.dto.ScopeDTO;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.model.Client;
import com.vapps.auth.model.Scope;
import com.vapps.auth.repository.ScopeRepository;

@Service
public class ScopeServiceImpl implements ScopeService {

    @Autowired
    private ScopeRepository scopeRepository;

    @Autowired
    private ClientService clientService;

    @Override
    public Optional<ScopeDTO> getScopesOfClient() {
        return Optional.empty();
    }

    @Override
    public Optional<ScopeDTO> getScope(String clientId, Long scopeId) {
        return Optional.empty();
    }

    @Override
    public Optional<ScopeDTO> getScope(String scopeName) {
        Optional<Scope> scope = scopeRepository.findByScopeNameIgnoreCase(scopeName);
        if (scope.isPresent()) {
            return Optional.of(scope.get().toDTO());
        }
        return Optional.empty();
    }

    @Override
    public String createScope(ClientDTO client, ScopeDTO scopeDetails) throws AppException {
        if(scopeRepository.findByClientIdAndScopeName(
                client.getId(), scopeDetails.getScopeName()).isPresent()) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Scope already present for this user");
        }
        Scope scope = new Scope();
        scope.setScopeName(scopeDetails.getScopeName());
        scope.setClient(Client.build(client));

        Scope addedScope = scopeRepository.save(scope);

        if(addedScope == null) {
            throw new AppException("Error while adding scope!");
        }
        return addedScope.getScopeId();
    }

    @Override
    public String deleteScope(String clientId, Long scopeId) {
        return null;
    }

    @Override
    public boolean isScopePresent(String clientId, Long scopeId) {
        return false;
    }

    @Override
    public List<String> deleteAllScopesOfClient(String userId, String clientId) {
        ClientDTO clientDTO = clientService.getClientById(userId, clientId);
        List<Scope> deletedScopes = scopeRepository.deleteByClientId(clientDTO.getId());
        List<String> deletedScopeIds = new ArrayList<>();

        if(deletedScopes == null) {
            deletedScopes.forEach(scope -> {
                deletedScopeIds.add(scope.getScopeId());
            });
        }
        return deletedScopeIds;
    }

    @Override
    public List<String> checkAndScopes(ClientDTO client, List<ScopeDTO> scopes) throws AppException {
        List<String> addedScopes = new ArrayList<>();
        for (ScopeDTO scope : scopes) {
            if(scopeRepository.findByClientIdAndScopeName(
                    client.getId(), scope.getScopeName()).isEmpty()) {
                addedScopes.add(createScope(client, scope));
            }
        }
        return addedScopes;
    }
}
