package com.vapps.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.Data;

@Document
@Data
public class Scope {
    
    @Id
	private String scopeId;
	@DocumentReference
	private Client client;
	private String scopeName;

}
