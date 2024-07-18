package com.vapps.auth.model;

import com.vapps.auth.dto.ScopeDTO;
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

	public ScopeDTO toDTO() {
		ScopeDTO scopeDTO = new ScopeDTO();
		scopeDTO.setScopeId(scopeId);
		scopeDTO.setScopeName(scopeName);
		scopeDTO.setClient(client.toDTO());
		return scopeDTO;
	}
}
