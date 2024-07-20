package com.vapps.auth.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient.Builder;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.base.Preconditions;
import com.vapps.auth.dto.ClientDTO;
import com.vapps.auth.dto.ScopeDTO;
import com.vapps.auth.dto.UserDTO;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.model.AppUser;
import com.vapps.auth.model.Client;
import com.vapps.auth.repository.ClientRepository;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ScopeService scopeService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private TokenSettings tokenSettings;

    @Autowired
    private ClientSettings clientSettings;

    @Value("${app.default.scopes}")
    private String defaultScopes;

    @Value("${app.client.clientIdPrefix}")
    private String clientIdPrefix;

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientServiceImpl.class);
    private static final Pattern CLIENT_NAME_REGEX = Pattern.compile("^[a-zA-Z0-9.]+$");

    @Override
    public String addClient(ClientDTO client) throws AppException {
        Assert.notNull(client, "Client can't be null");
        Client addedClient = clientRepository.save(Client.build(client));
        if (addedClient != null) {
            return addedClient.getId();
        }
        throw new AppException("Can't create the client");
    }

    @Override
    public String addClient(String userId, Builder registeredClient) throws AppException {
        UserDTO appUser = appUserService.findByUserId(userId);
        if (appUser == null) {
            throw new AppException("User not found for " + userId);
        }
        String clientId = UUID.randomUUID().toString();
        registeredClient.clientId(clientIdPrefix + "-" + clientId.toString()).tokenSettings(tokenSettings)
                .clientSettings(clientSettings);

        Client client = Client.build(registeredClient.build());

        if (client.getClientName() == null || !CLIENT_NAME_REGEX.matcher(client.getClientName()).matches()) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid client name!");
        }

        if (clientRepository.findByClientNameIgnoreCase(client.getClientName()).isPresent()) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Client already existing with this name");
        }
        client.setUser(AppUser.build(appUser));
        formatScopes(client);
        Client addedClient = clientRepository.save(client);

        if (addedClient != null) {
            LOGGER.info("Adding scopes to db ...");
            addScopes(addedClient, addedClient.getScopes());
            return addedClient.getClientId();
        }
        throw new AppException("Can't create the client");
    }

    @Override
    public boolean isClientExists(String clientId) {
        Assert.notNull(clientId, "Client id can't be null");
        return clientRepository.findByClientId(clientId).isPresent();
    }

    @Override
    public boolean isClientExistsByName(String clientName) {
        return clientRepository.findByClientNameIgnoreCase(clientName).isPresent();
    }

    @Override
    public void removeClient(String userId, String clientId) {
        Assert.notNull(clientId, "Client id can't be null");
        scopeService.deleteAllScopesOfClient(userId, clientId);
        clientRepository.deleteByClientId(clientId);
    }

    @Override
    public List<ClientDTO> getAllClients(String userId) {
        Preconditions.checkArgument(userId != null, "User id required");

        List<Client> clients = clientRepository.findByUserId(userId);
        return clients.stream().map(client -> client.toDTO()).collect(Collectors.toList());
    }

    @Override
    public ClientDTO getClientById(String userId, String clientId) {
        Client client = clientRepository.findByClientIdAndUserId(clientId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Client id not exists"));

        return client.toDTO();
    }

    @Override
    public Optional<ClientDTO> getClientByClientId(String clientId) {
        Optional<Client> client = clientRepository.findByClientId(clientId);
        if (client.isPresent()) {
            return Optional.of(client.get().toDTO());
        }
        return Optional.empty();
    }

    @Override
    public void updateClient(String userId, ClientDTO clientDetails) throws AppException {
        Preconditions.checkArgument(clientDetails != null, "Client data can't be empty");
        Preconditions.checkArgument(userId != null && !userId.isBlank(), "User id can't be empty");

        Optional<Client> prevClient = clientRepository.findByClientIdAndUserId(clientDetails.getClientId(), userId);
        if (prevClient.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Client not exists");
        }
        if (clientDetails.getClientName() != null) {
            Optional<Client> temp = clientRepository.findByClientNameIgnoreCase(clientDetails.getClientName());
            if (temp.isPresent() && !temp.get().getClientName().equals(clientDetails.getClientName())) {
                throw new AppException(HttpStatus.BAD_REQUEST.value(), "Client already existing with this name");
            }
        }

        clientDetails.setUserDTO(prevClient.get().getUser().toDTO());
        Client newClient = Client.build(clientDetails);
        formatScopes(newClient);
        checkAndUpdateClient(prevClient.get(), newClient);

        //TODO Need to validate the client details before updating
        clientRepository.save(newClient);
    }

    private void addScopes(Client client, String scopes) throws AppException {
        List<ScopeDTO> scopeDetails = Arrays.stream(scopes.split(",")).map((final String scope) -> {
            ScopeDTO scopeDetail = new ScopeDTO();
            scopeDetail.setScopeName(scope);
            scopeDetail.setClient(client.toDTO());
            return scopeDetail;
        }).toList();
        scopeService.checkAndScopes(client.toDTO(), scopeDetails);
    }

    private void formatScopes(Client client) {
        StringBuffer scopes = new StringBuffer();
        for (String scope : client.getScopes().split(",")) {
            if (!Arrays.stream(defaultScopes.split(","))
                    .anyMatch(defaultScope -> defaultScope.equalsIgnoreCase(scope)) && !scope.toLowerCase()
                    .startsWith((client.getClientName() + ".").toLowerCase()) && scopeService.getScope(scope)
                    .isEmpty()) {
                /**
                 * If scope is not a default scope, not already prefixed with client name and
                 * not a scope of other client too. Then prefix the client name with the scope.
                 */
                scopes.append(client.getClientName() + "." + scope).append(",");
                continue;
            }
            scopes.append(scope).append(",");
        }
        if (scopes.length() > 0) {
            scopes.deleteCharAt(scopes.length() - 1);
        }
        client.setScopes(scopes.toString());
    }

    private void checkAndUpdateClient(Client prevClient, Client newClient) {
        if (newClient.getId() == null) {
            newClient.setId(prevClient.getId());
        }
        if (newClient.getClientSecret() == null) {
            newClient.setClientSecret(prevClient.getClientSecret());
        }
        if (newClient.getClientIdIssuedAt() == null) {
            newClient.setClientIdIssuedAt(prevClient.getClientIdIssuedAt());
        }
        if (newClient.getClientName() == null) {
            newClient.setClientName(prevClient.getClientName());
        }
        if (newClient.getClientSettings() == null) {
            newClient.setClientSettings(prevClient.getClientSettings());
        }
        if (newClient.getAuthorizationGrantTypes() == null) {
            newClient.setAuthorizationGrantTypes(prevClient.getAuthorizationGrantTypes());
        }
        if (newClient.getClientAuthenticationMethods() == null) {
            newClient.setClientAuthenticationMethods(prevClient.getClientAuthenticationMethods());
        }
        if (newClient.getClientSecretExpiresAt() == null) {
            newClient.setClientSecretExpiresAt(prevClient.getClientSecretExpiresAt());
        }
        if (newClient.getRedirectUris() == null) {
            newClient.setRedirectUris(prevClient.getRedirectUris());
        }
        if (newClient.getScopes() == null) {
            newClient.setScopes(prevClient.getScopes());
        }
        if (newClient.getTokenSettings() == null) {
            newClient.setTokenSettings(prevClient.getTokenSettings());
        }
        if (newClient.getUser() == null) {
            newClient.setUser(prevClient.getUser());
        }
    }

}
