package com.vapps.auth.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.web.bind.annotation.*;

import com.vapps.auth.dto.ClientControllerResponse;
import com.vapps.auth.dto.ClientCreationPayload;
import com.vapps.auth.dto.ClientDTO;
import com.vapps.auth.dto.ClientResponseWithId;
import com.vapps.auth.dto.EmptyResponse;
import com.vapps.auth.dto.SingleClientControllerResponse;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.service.ClientService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/{userId}/client")
public class ClientController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private ClientService clientService;

	private void getClientAuth(Set<ClientAuthenticationMethod> clientAuthenticationMethods) {
		clientAuthenticationMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
		clientAuthenticationMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_POST);
		clientAuthenticationMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_JWT);
	}

	private void getGrantTypes(Set<AuthorizationGrantType> authorizationGrantTypes) {
		authorizationGrantTypes.add(AuthorizationGrantType.AUTHORIZATION_CODE);
		authorizationGrantTypes.add(AuthorizationGrantType.CLIENT_CREDENTIALS);
		authorizationGrantTypes.add(AuthorizationGrantType.DEVICE_CODE);
		authorizationGrantTypes.add(AuthorizationGrantType.REFRESH_TOKEN);
		authorizationGrantTypes.add(AuthorizationGrantType.JWT_BEARER);
	}

	@PostMapping
	public ResponseEntity<?> addClient(@RequestBody ClientCreationPayload payload, Principal principal)
			throws AppException {

		if (principal == null || principal.getName() == null) {
			throw new AppException(401, "User details not found");
		}
		List<String> redirectUris = Arrays.asList(payload.getRedirectUris().split(","));
		List<String> scopes = Arrays.asList(payload.getScopes().split(","));

		String secret = payload.getClientSecret();
		secret = passwordEncoder.encode(secret);
		
		RegisteredClient.Builder client = RegisteredClient.withId(UUID.randomUUID().toString()).clientSecret(secret)
				.clientAuthenticationMethods(this::getClientAuth).clientName(payload.getClientName())
				.scopes(scope -> scopes.forEach(scope::add))
				.redirectUris(redirect -> redirectUris.forEach(redirect::add))
				.authorizationGrantTypes(this::getGrantTypes);

		String clientId = clientService.addClient(principal.getName(), client);

		ClientResponseWithId response = new ClientResponseWithId();
		response.setClientId(clientId);
		response.setMessage("success");
		response.setStatus(HttpStatus.CREATED.value());
		response.setPath("/api/user/" + principal.getName() + "/client");
		response.setTimestamp(LocalDateTime.now());

		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<?> getAllClient(@PathVariable("userId") String userId, Principal principal,
			HttpServletRequest request) throws AppException {

		if (principal == null || principal.getName() == null || principal.getName().isEmpty()) {
			throw new AppException(401, "Not Authenticated");
		}
		List<ClientDTO> clients = clientService.getAllClients(userId);
		ClientControllerResponse clientResponse = new ClientControllerResponse(HttpStatus.OK.value(), "success",
				LocalDateTime.now(), request.getServletPath(), clients);

		return ResponseEntity.ok(clientResponse);
	}

	@GetMapping("{clientId}")
	public ResponseEntity<?> getClient(@PathVariable("userId") String userId, @PathVariable("clientId") String clientId,
			Principal principal, HttpServletRequest request) throws AppException {

		if (principal == null || principal.getName() == null || principal.getName().isEmpty()) {
			throw new AppException(401, "Not Authenticated");
		}
		ClientDTO client = clientService.getClientById(userId, clientId);
		SingleClientControllerResponse clientResponse = new SingleClientControllerResponse(HttpStatus.OK.value(),
				"success", LocalDateTime.now(), request.getServletPath(), client);

		return ResponseEntity.ok(clientResponse);
	}

	@DeleteMapping("{clientId}")
	public ResponseEntity<?> deleteClient(@PathVariable("userId") String userId,
			@PathVariable("clientId") String clientId, Principal principal) throws AppException {
		if (principal == null || principal.getName() == null || principal.getName().isEmpty()) {
			throw new AppException(401, "Not Authenticated");
		}
		clientService.removeClient(userId, clientId);

		EmptyResponse response = new EmptyResponse();
		response.setMessage("Successfully deleted client");
		response.setPath("/api/user/" + userId + "/client/" + clientId);
		response.setStatus(HttpStatus.OK.value());
		response.setTimestamp(LocalDateTime.now());

		return ResponseEntity.ok(response);
	}

	@PutMapping("{clientId}")
	public ResponseEntity<?> updateClient(@PathVariable("userId") String userId,
			@PathVariable("clientId") String clientId, @RequestBody ClientDTO clientDetails,
			Principal principal) throws AppException {
		clientDetails.setClientId(clientId);
		clientService.updateClient(userId, clientDetails);

		EmptyResponse response = new EmptyResponse();
		response.setMessage("Updated client successfully");
		response.setTimestamp(LocalDateTime.now());
		response.setPath("/api/user/" + userId + "/client/" + clientId);
		response.setStatus(HttpStatus.OK.value());

		return ResponseEntity.ok(response);
	}
}
