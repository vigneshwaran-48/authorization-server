package com.vapps.auth.controller;

import com.vapps.auth.dto.*;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.service.AppUserService;
import com.vapps.auth.service.ClientService;
import com.vapps.auth.service.ScopeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    @Autowired
    private OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService;

    @Autowired
    private AppUserService userService;

    @Autowired
    private ScopeService scopeService;

    @Autowired
    private ClientService clientService;

    private static final String SCOPES_SEPARATOR = " ";

    @GetMapping("/consent")
    public ResponseEntity<ConsentResponse> oauthConsent(@RequestParam String clientId, @RequestParam String scopes,
            HttpServletRequest request, Principal principal) throws AppException {
        UserDTO userDTO = userService.findByUserId(principal.getName());

        List<ConsentDTO.ClientScopeDetails> clientScopeDetailsList = new ArrayList<>();

        ClientDTO clientDTO = clientService.getClientByClientId(clientId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST.value(), "Client not exists!"));

        OAuth2AuthorizationConsent oAuth2AuthorizationConsent =
                oAuth2AuthorizationConsentService.findById(clientDTO.getId(), principal.getName());

        for (String scope : scopes.split(SCOPES_SEPARATOR)) {
            Optional<ScopeDTO> scopeDTO = scopeService.getScope(scope);
            if (scopeDTO.isEmpty()) {
                // Ignoring scope that doesn't exist!
                continue;
            }
            ConsentDTO.ClientScopeDetails clientScopeDetails = clientScopeDetailsList.stream()
                    .filter(clientScope -> clientScope.getClientId().equals(scopeDTO.get().getClient().getClientId()))
                    .findFirst().orElse(null);

            if (clientScopeDetails == null) {
                clientScopeDetails = new ConsentDTO.ClientScopeDetails();
                clientScopeDetails.setClientId(scopeDTO.get().getClient().getClientId());
                clientScopeDetails.setClientName(scopeDTO.get().getClient().getClientName());
                clientScopeDetails.setScopeDetails(new ArrayList<>());
                clientScopeDetailsList.add(clientScopeDetails);
            }

            ConsentDTO.ScopeDetails scopeDetails = new ConsentDTO.ScopeDetails();
            scopeDetails.setDescription(scope); // TODO After adding scope's description feature need to change this.
            scopeDetails.setName(scope);
            if (oAuth2AuthorizationConsent != null) {
                scopeDetails.setAlreadyAllowed(oAuth2AuthorizationConsent.getScopes().contains(scope));
            }

            List<ConsentDTO.ScopeDetails> scopeDetailsList = clientScopeDetails.getScopeDetails();
            scopeDetailsList.add(scopeDetails);
            clientScopeDetails.setScopeDetails(scopeDetailsList);
        }
        ConsentDTO consentDTO = new ConsentDTO();
        consentDTO.setClientId(clientId);
        consentDTO.setClientScopes(clientScopeDetailsList);
        consentDTO.setClientName(clientDTO.getClientName());
        consentDTO.setUserEmail(userDTO.getEmail());
        consentDTO.setUserName(userDTO.getUserName());

        ConsentResponse response = new ConsentResponse();
        response.setConsentData(consentDTO);
        response.setPath(request.getServletPath());
        response.setMessage("success");
        response.setTimestamp(LocalDateTime.now());
        response.setStatus(HttpStatus.OK.value());

        return ResponseEntity.ok(response);
    }
}
