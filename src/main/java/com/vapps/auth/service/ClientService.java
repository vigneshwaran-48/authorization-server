package com.vapps.auth.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import com.vapps.auth.dto.ClientDTO;
import com.vapps.auth.exception.AppException;

public interface ClientService {

    String addClient(ClientDTO client) throws AppException;
	
	boolean isClientExists(String clientId);

	boolean isClientExistsByName(String clientName);
	
	void removeClient(String userId, String clientId);

	/**
	 * This method will return the client id not the table's primary key id
	 * @param registeredClient
	 * @return
	 * @throws Exception
	 */
	String addClient(String userId, RegisteredClient.Builder registeredClient) throws AppException;
	
	List<ClientDTO> getAllClients(String userId);
	
	ClientDTO getClientById(String userId, String clientId);
	Optional<ClientDTO> getClientByClientId(String clientId);
	void updateClient(String userId, ClientDTO commonClientDetails) throws AppException;
}
