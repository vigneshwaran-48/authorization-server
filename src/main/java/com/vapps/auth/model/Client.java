package com.vapps.auth.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vapps.auth.dto.ClientDTO;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Document
@Data
public class Client {
    
    @Id
    private String id;
    private String clientId;
    private Instant clientIdIssuedAt;
	private String clientSecret;
	private Instant clientSecretExpiresAt;
	private String clientName;
    @Size(max = 1000)
	private String clientAuthenticationMethods;
	@Size(max = 1000)
	private String authorizationGrantTypes;
	@Size(max = 1000)
	private String redirectUris;
	@Size(max = 1000)
	private String scopes;
	@Size(max = 2000)
	private String clientSettings;
	@Size(max = 2000)
	private String tokenSettings;
	@DocumentReference
	private AppUser user;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Client build(ClientDTO clientDTO) {
        Client client = new Client();
        client.setId(clientDTO.getId());
        client.setAuthorizationGrantTypes(clientDTO.getAuthorizationGrantTypes());
        client.setClientAuthenticationMethods(clientDTO.getClientAuthenticationMethods());
        client.setClientId(clientDTO.getClientId());
        client.setClientIdIssuedAt(clientDTO.getClientIdIssuedAt());
        client.setClientName(clientDTO.getClientName());
        client.setClientSecret(clientDTO.getClientSecret());
        client.setClientSecretExpiresAt(clientDTO.getClientSecretExpiresAt());
        client.setClientSettings(clientDTO.getClientSettings());
        client.setRedirectUris(clientDTO.getRedirectUris());
        client.setScopes(clientDTO.getScopes());
        client.setTokenSettings(clientDTO.getTokenSettings());
        return client;
    }

    public static Client build(RegisteredClient registeredClient) {
        List<String> clientAuthMethods = registeredClient.getClientAuthenticationMethods().stream()
				.map(ClientAuthenticationMethod::getValue).collect(Collectors.toList());
		List<String> authorizationGrantTypes = registeredClient.getAuthorizationGrantTypes().stream()
				.map(AuthorizationGrantType::getValue).collect(Collectors.toList());

		Client client = new Client();
		client.setId(registeredClient.getId());
		client.setClientId(registeredClient.getClientId());
		client.setClientName(registeredClient.getClientName());
		client.setClientSecret(registeredClient.getClientSecret());
		client.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
		client.setClientIdIssuedAt(registeredClient.getClientIdIssuedAt());
		client.setClientAuthenticationMethods(StringUtils.collectionToCommaDelimitedString(clientAuthMethods));
		client.setAuthorizationGrantTypes(StringUtils.collectionToCommaDelimitedString(authorizationGrantTypes));
		client.setRedirectUris(StringUtils.collectionToCommaDelimitedString(registeredClient.getRedirectUris()));
		client.setScopes(StringUtils.collectionToCommaDelimitedString(registeredClient.getScopes()));
		client.setClientSettings(writeMap(registeredClient.getClientSettings().getSettings()));
		client.setTokenSettings(writeMap(registeredClient.getTokenSettings().getSettings()));

        return client;
    }

	public ClientDTO toDTO() {
		ClientDTO clientDTO = new ClientDTO();
		clientDTO.setAuthorizationGrantTypes(authorizationGrantTypes);
		clientDTO.setClientAuthenticationMethods(clientAuthenticationMethods);
		clientDTO.setClientId(clientId);
		clientDTO.setClientIdIssuedAt(clientIdIssuedAt);
		clientDTO.setClientName(clientName);
		clientDTO.setClientSecret(clientSecret);
		clientDTO.setClientSecretExpiresAt(clientSecretExpiresAt);
		clientDTO.setClientSettings(clientSettings);
		clientDTO.setId(id);
		clientDTO.setRedirectUris(redirectUris);
		clientDTO.setScopes(scopes);
		clientDTO.setTokenSettings(tokenSettings);
		return clientDTO;
	}

    private static String writeMap(Map<String, Object> data) {
		try {
			return objectMapper.writeValueAsString(data);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}
	
	static {
		ClassLoader classLoader = Client.class.getClassLoader();
		List<com.fasterxml.jackson.databind.Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
		objectMapper.registerModules(securityModules);
		objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
	}
}
