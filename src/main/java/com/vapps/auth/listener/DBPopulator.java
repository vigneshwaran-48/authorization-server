package com.vapps.auth.listener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import com.vapps.auth.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Component;

import com.vapps.auth.dto.UserDTO;
import com.vapps.auth.dto.UserProfileImageDTO;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.service.AppUserService;
import com.vapps.auth.service.UserProfileImageService;
import com.vapps.auth.util.OAuth2Provider;

@Component
public class DBPopulator {

    @Autowired
    private UserProfileImageService userProfileImageService;

    @Autowired
    private AppUserService userService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ClientService clientService;

    @Value("${app.default.scopes}")
    private String defaultScopes;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DBPopulator.class);
    private static final String DEFAULT_USER_ID = "-1";
    private static final String DEFAULT_IMAGE_LOCATION = "classpath:static/person.png";

    @EventListener(ContextRefreshedEvent.class)
    public void onEvent() {
        try {
            checkAndAddDefaultUser();
            if (userProfileImageService.getDefaultImage().isEmpty()) {
                storeDefaultUserImage();
            }
            storeDefaultClient();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void storeDefaultClient() throws AppException {
        if (clientService.isClientExistsByName("Default")) {
            LOGGER.info("Default client exists!");
            return;
        }
        RegisteredClient.Builder defaultClientBuilder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientName("Default")
                .clientId("oidc-client")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://auth-server:9000/login/oauth2/code/default-client")
                .postLogoutRedirectUri("http://127.0.0.1:8080/")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build());
        for (String defaultScope : defaultScopes.split(",")) {
            defaultClientBuilder.scope(defaultScope);
        }
        String defaultClientId = clientService.addClient(DEFAULT_USER_ID, defaultClientBuilder);
        LOGGER.info("Created default client {}", defaultClientId);
    }

    private void checkAndAddDefaultUser() throws AppException {
        try {
            userService.findByUserId(DEFAULT_USER_ID);
            LOGGER.info("Default user exists!");
        } catch (AppException e) {
            LOGGER.info("Default user not exists so creating it!");
            storeDefaultUser();
        }
    }

    private void storeDefaultUser() throws AppException {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(DEFAULT_USER_ID);
        userDTO.setEmail("default@vapps.com");
        userDTO.setPassword("default");
        userDTO.setUserName("Default");
        userDTO.setProvider(OAuth2Provider.VAPPS);
        userService.createUser(userDTO);
        LOGGER.info("Created default user!");
    }

    private void storeDefaultUserImage() throws AppException {
        UserDTO userDTO = userService.findByUserId(DEFAULT_USER_ID);
        UserProfileImageDTO profileImageDTO = new UserProfileImageDTO();
        profileImageDTO.setType("image/png");
        profileImageDTO.setUserDetails(userDTO);
        profileImageDTO.setId("-1");
        try {
            Resource defaultProfileImage = resourceLoader.getResource(DEFAULT_IMAGE_LOCATION);
            profileImageDTO.setImageBytes(Files.readAllBytes(Paths.get(defaultProfileImage.getURI())));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new AppException("Default profile image file not found!");
        }
        userProfileImageService.uploadImage(profileImageDTO);
        LOGGER.info("Created default user image!");
    }
}
