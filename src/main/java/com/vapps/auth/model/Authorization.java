package com.vapps.auth.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Document
@Data
public class Authorization {
    
    @Id
	private String id;
	private String registeredClientId;
	private String principalName;
	private String authorizationGrantType;
	
	@Size(max = 1000)
	private String authorizedScopes;
	
	@Size(max = 4000)
	private String attributes;
	
	@Size(max = 500)
	private String state;
	
	@Size(max = 4000)
	private String authorizationCodeValue;
	private Instant authorizationCodeIssuedAt;
	private Instant authorizationCodeExpiresAt;
	private String authorizationCodeMetadata;
	
	@Size(max = 4000)
	private String accessTokenValue;
	private Instant accessTokenIssuedAt;
	private Instant accessTokenExpiresAt;
	
	@Size(max = 2000)
	private String accessTokenMetadata;
	private String accessTokenType;
	
	@Size(max = 1000)
	private String accessTokenScopes;
	
	@Size(max = 4000)
	private String refreshTokenValue;
	private Instant refreshTokenIssuedAt;
	private Instant refreshTokenExpiresAt;
	
	@Size(max = 2000)
	private String refreshTokenMetadata;
	@Size(max = 4000)
	private String oidcIdTokenValue;
	private Instant oidcIdTokenIssuedAt;
	private Instant oidcIdTokenExpiresAt;
	@Size(max = 2000)
	private String oidcIdTokenMetadata;
	@Size(max = 2000)
	private String oidcIdTokenClaims;
}
