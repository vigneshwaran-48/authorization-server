package com.vapps.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class AuthorizationConsent {
    
    @Id
    private String id;

    @Indexed(unique = true)
	private String registeredClientId;

	@Indexed(unique = true)
	private String principalName;
	
	private String authorities;
    
}
