package com.vapps.auth.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient.Builder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.base.Preconditions;
import com.vapps.auth.dto.ClientDTO;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.model.AppUser;
import com.vapps.auth.model.Client;
import com.vapps.auth.repository.ClientRepository;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
	private ClientRepository clientRepository;

    @Autowired
    private ScopeService scopeService;

    @Override
    public String addClient(ClientDTO client) throws AppException {
        Assert.notNull(client, "Client can't be null");
		Client addedClient = clientRepository.save(Client.build(client));
		if(addedClient != null) {
			return addedClient.getId();
		}
		throw new AppException("Can't create the client");
    }

    @Override
    public String addClient(String userId, Builder registeredClient) throws AppException {
        Client addedClient = clientRepository.save(Client.build(registeredClient.build()));
		if(addedClient != null) {
			return addedClient.getId();
		}
		throw new AppException("Can't create the client");
    }

    @Override
    public boolean isClientExists(String clientId) {
        Assert.notNull(clientId, "Client id can't be null");
		return clientRepository.findByClientId(clientId).isPresent();
    }

    @Override
    public void removeClient(String userId, String clientId) {
        Assert.notNull(clientId, "Client id can't be null");
		scopeService.deleteAllScopesOfClient(clientId);
		clientRepository.deleteByClientId(clientId);
    }

    @Override
    public List<ClientDTO> getAllClients(String userId) {
        Preconditions.checkArgument(userId != null, "User id required");
		
		List<Client> clients = clientRepository.findByUserId(userId);
		return clients.stream()
					  .map(client -> client.toDTO())
					  .collect(Collectors.toList());
    }

    @Override
    public ClientDTO getClientById(String userId, String clientId) {
        Client client = clientRepository.findByClientIdAndUserId(clientId, userId)
					.orElseThrow(() -> new IllegalArgumentException("Client id not exists"));
		
		return client.toDTO();
    }

    @Override
    public void updateClient(String userId, ClientDTO clientDetails) throws AppException {
        Preconditions.checkArgument(clientDetails != null,
									"Client data can't be empty");
		Preconditions.checkArgument(userId != null && !userId.isBlank(),
				"User id can't be empty");

		Optional<Client> prevClient =
					clientRepository
							.findByClientIdAndUserId(
									clientDetails.getClientId(),
									userId);
		if(prevClient.isEmpty()) {
			throw new AppException(HttpStatus.BAD_REQUEST.value(), "Client not exists");
		}
		if(clientDetails.getClientName() != null) {
			Optional<Client> temp = clientRepository
					.findByUserIdAndClientName(userId, clientDetails.getClientName());
			if(temp.isPresent() && !temp.get().getClientName().equals(clientDetails.getClientName())) {
				throw new AppException(HttpStatus.BAD_REQUEST.value(), "Client already existing with this name");
			}
		}

		clientDetails.setUserDTO(prevClient.get().getUser().toDTO());
		Client newClient = Client.build(clientDetails);
		checkAndUpdateClient(prevClient.get(), newClient);

		//TODO Need to validate the client details before updating
		clientRepository.save(newClient);
    }

    private void checkAndUpdateClient(Client prevClient, Client newClient) {
		if(newClient.getId() == null) {
			newClient.setId(prevClient.getId());
		}
		if(newClient.getClientSecret() == null) {
			newClient.setClientSecret(prevClient.getClientSecret());
		}
		if(newClient.getClientIdIssuedAt() == null) {
			newClient.setClientIdIssuedAt(prevClient.getClientIdIssuedAt());
		}
		if(newClient.getClientName() == null) {
			newClient.setClientName(prevClient.getClientName());
		}
		if(newClient.getClientSettings() == null) {
			newClient.setClientSettings(prevClient.getClientSettings());
		}
		if(newClient.getAuthorizationGrantTypes() == null) {
			newClient.setAuthorizationGrantTypes(prevClient.getAuthorizationGrantTypes());
		}
		if(newClient.getClientAuthenticationMethods() == null) {
			newClient.setClientAuthenticationMethods(
					prevClient.getClientAuthenticationMethods());
		}
		if(newClient.getClientSecretExpiresAt() == null) {
			newClient.setClientSecretExpiresAt(prevClient.getClientSecretExpiresAt());
		}
		if(newClient.getRedirectUris() == null) {
			newClient.setRedirectUris(prevClient.getRedirectUris());
		}
		if(newClient.getScopes() == null) {
			newClient.setScopes(prevClient.getScopes());
		}
		if(newClient.getTokenSettings() == null) {
			newClient.setTokenSettings(prevClient.getTokenSettings());
		}
		if(newClient.getUser() == null) {
			newClient.setUser(prevClient.getUser());
		}
	}
    
}
