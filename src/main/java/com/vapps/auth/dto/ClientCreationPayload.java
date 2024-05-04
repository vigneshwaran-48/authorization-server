package com.vapps.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientCreationPayload {

	private String redirectUris, scopes, clientName, grantTypes, clientId, clientSecret, userId;

}
