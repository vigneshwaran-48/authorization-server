package com.vapps.auth.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class ClientDTO {
    private String id;
	private String clientId;
	private Instant clientIdIssuedAt;
	private String clientSecret;
	private Instant clientSecretExpiresAt;
	private String clientName;
	private String clientAuthenticationMethods;
	private String authorizationGrantTypes;
	private String redirectUris;
	private String scopes;
	private String clientSettings;
	private String tokenSettings;
	private UserDTO userDTO;
}
