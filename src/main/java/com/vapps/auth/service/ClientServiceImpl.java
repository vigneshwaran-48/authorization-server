package com.vapps.auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient.Builder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.vapps.auth.dto.ClientDTO;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.model.Client;
import com.vapps.auth.repository.ClientRepository;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
	private ClientRepository clientRepository;

    @Override
    public String addClient(ClientDTO client) throws AppException {
        Assert.notNull(client, "Client can't be null");
		Client addedClient = clientRepository.save(Client.build(client));
		if(addedClient != null) {
			return addedClient.getId();
		}
		throw new AppException("Unable to create the client");
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
		clientRepository.deleteByClientId(clientId);
    }

    @Override
    public List<ClientDTO> getAllClients(String userId) {
        // TODO Should implement this
		return null;
    }

    @Override
    public ClientDTO getClientById(String userId, String clientId) {
        /// TODO Should implement this
		return null;
    }

    @Override
    public void updateClient(String userId, ClientDTO commonClientDetails) throws AppException {
        // TODO Should implement this
    }
    
}
