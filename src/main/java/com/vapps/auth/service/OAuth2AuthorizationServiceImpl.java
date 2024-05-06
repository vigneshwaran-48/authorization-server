package com.vapps.auth.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vapps.auth.model.Authorization;
import com.vapps.auth.repository.AuthorizationRepository;

@Service
public class OAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {

    @Autowired
    private AuthorizationRepository authorizationRepository;
    
    @Autowired
	private RegisteredClientRepository registeredClientRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2AuthorizationServiceImpl.class);

    public OAuth2AuthorizationServiceImpl() {
        ClassLoader classLoader = OAuth2AuthorizationServiceImpl.class.getClassLoader();
        List<com.fasterxml.jackson.databind.Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        this.objectMapper.registerModules(securityModules);
        this.objectMapper.registerModule(new CoreJackson2Module());
        this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        Authorization entityAuthorization = toAuthorizationObject(authorization);
        authorizationRepository.save(entityAuthorization);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");
        authorizationRepository.deleteById(authorization.getId());
    }

    @Override
	public OAuth2Authorization findById(String id) {
		Assert.hasText(id, "Id cannot be empty");
		Authorization authorization = authorizationRepository.findById(id).orElse(null);
		return toOAuth2Authorization(authorization);
	}

	@Override
	public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
		Assert.hasText(token, "Token cannot be empty");
		Optional<Authorization> retrieved = null;
		if(tokenType == null) {
			retrieved = authorizationRepository.findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValue(token);
		}
		else if(OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
			retrieved = authorizationRepository.findByState(token);
		}
		else if(OAuth2ParameterNames.ACCESS_TOKEN.equals(tokenType.getValue())) {
			retrieved = authorizationRepository.findByAccessTokenValue(token);
		}
		else if(OAuth2ParameterNames.REFRESH_TOKEN.equals(tokenType.getValue())) {
			retrieved = authorizationRepository.findByRefreshTokenValue(token);
		}
		else if(OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
			retrieved = authorizationRepository.findByAuthorizationCodeValue(token);
		}
		else {
			retrieved = Optional.empty();
		}
		return retrieved.map(this::toOAuth2Authorization).orElse(null);
	}

    private OAuth2Authorization toOAuth2Authorization(Authorization authorization) {
        RegisteredClient registeredClient = registeredClientRepository.findById(authorization.getRegisteredClientId());
        if (registeredClient == null) {
            throw new DataRetrievalFailureException("The RegisteredClient with id '"
                    + authorization.getRegisteredClientId() + "' was not found in the RegisteredClientRepository.");
        }

        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .id(authorization.getId()).principalName(authorization.getPrincipalName())
                .authorizationGrantType(resolveAuthorizationGrantType(authorization.getAuthorizationGrantType()))
                .authorizedScopes(StringUtils.commaDelimitedListToSet(authorization.getAuthorizedScopes()))
                .attributes(attribute -> attribute.putAll(parseMap(authorization.getAttributes())));

        if (authorization.getState() != null) {
            builder.attribute(OAuth2ParameterNames.STATE, authorization.getState());
        }
        if (authorization.getAuthorizationCodeValue() != null) {
            OAuth2AuthorizationCode authCode = new OAuth2AuthorizationCode(authorization.getAuthorizationCodeValue(),
                    authorization.getAuthorizationCodeIssuedAt(), authorization.getAuthorizationCodeExpiresAt());
            builder.token(authCode,
                    metadata -> metadata.putAll(parseMap(authorization.getAuthorizationCodeMetadata())));
        } else {
            LOGGER.info("Not a authorization code");
        }

        if (authorization.getAccessTokenValue() != null) {
            OAuth2AccessToken accessToken =
                    new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, authorization.getAccessTokenValue(),
                            authorization.getAccessTokenIssuedAt(), authorization.getAccessTokenExpiresAt(),
                            StringUtils.commaDelimitedListToSet(authorization.getAccessTokenScopes()));
            builder.token(accessToken, metadata -> metadata.putAll(parseMap(authorization.getAccessTokenMetadata())));
        } else {
            LOGGER.info("Not a Access token");
        }

        if (authorization.getRefreshTokenValue() != null) {
            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(authorization.getRefreshTokenValue(),
                    authorization.getRefreshTokenIssuedAt(), authorization.getRefreshTokenExpiresAt());
            builder.token(refreshToken, metadata -> metadata.putAll(parseMap(authorization.getRefreshTokenMetadata())));
        } else {
            LOGGER.info("Not a Refresh token");
        }

        if (authorization.getOidcIdTokenValue() != null) {
            OidcIdToken idToken =
                    new OidcIdToken(authorization.getOidcIdTokenValue(), authorization.getOidcIdTokenIssuedAt(),
                            authorization.getOidcIdTokenExpiresAt(), parseMap(authorization.getOidcIdTokenClaims()));
            builder.token(idToken, metadata -> metadata.putAll(parseMap(authorization.getOidcIdTokenMetadata())));
        } else {
            LOGGER.info("Not a OIDC token");
        }
        
        return builder.build();
    }

    private Authorization toAuthorizationObject(OAuth2Authorization authorization) {
        Authorization entityAuth = new Authorization();
        entityAuth.setId(authorization.getId());
        entityAuth.setRegisteredClientId(authorization.getRegisteredClientId());
        entityAuth.setPrincipalName(authorization.getPrincipalName());
        entityAuth.setAuthorizationGrantType(authorization.getAuthorizationGrantType().getValue());
        entityAuth
                .setAuthorizedScopes(StringUtils.collectionToDelimitedString(authorization.getAuthorizedScopes(), ","));
        entityAuth.setAttributes(writeMap(authorization.getAttributes()));
        entityAuth.setState(authorization.getAttribute(OAuth2ParameterNames.STATE));

        OAuth2Authorization.Token<OAuth2AuthorizationCode> code = authorization.getToken(OAuth2AuthorizationCode.class);
        setTokenValue(code, entityAuth::setAuthorizationCodeValue, entityAuth::setAuthorizationCodeIssuedAt,
                entityAuth::setAuthorizationCodeExpiresAt, entityAuth::setAuthorizationCodeMetadata);

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getToken(OAuth2AccessToken.class);
        setTokenValue(accessToken, entityAuth::setAccessTokenValue, entityAuth::setAccessTokenIssuedAt,
                entityAuth::setAccessTokenExpiresAt, entityAuth::setAccessTokenMetadata);

        if (accessToken != null && accessToken.getToken().getScopes() != null) {
            entityAuth.setAccessTokenScopes(
                    StringUtils.collectionToDelimitedString(accessToken.getToken().getScopes(), ","));
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getToken(OAuth2RefreshToken.class);
        setTokenValue(refreshToken, entityAuth::setRefreshTokenValue, entityAuth::setRefreshTokenIssuedAt,
                entityAuth::setRefreshTokenExpiresAt, entityAuth::setRefreshTokenMetadata);

        OAuth2Authorization.Token<OidcIdToken> oidcToken = authorization.getToken(OidcIdToken.class);
        setTokenValue(oidcToken, entityAuth::setOidcIdTokenValue, entityAuth::setOidcIdTokenIssuedAt,
                entityAuth::setOidcIdTokenExpiresAt, entityAuth::setOidcIdTokenMetadata);

        if (oidcToken != null) {
            entityAuth.setOidcIdTokenClaims(writeMap(oidcToken.getClaims()));
        }

        return entityAuth;
    }

    private void setTokenValue(OAuth2Authorization.Token<?> token, Consumer<String> tokenValueConsumer,
            Consumer<Instant> issuedAtConsumer, Consumer<Instant> expiredAtConsumer,
            Consumer<String> metaDataConsumer) {
        if (token != null) {
            OAuth2Token oAuth2Token = token.getToken();
            tokenValueConsumer.accept(oAuth2Token.getTokenValue());
            issuedAtConsumer.accept(oAuth2Token.getIssuedAt());
            expiredAtConsumer.accept(oAuth2Token.getExpiresAt());
            metaDataConsumer.accept(writeMap(token.getMetadata()));
        }
    }

    private Map<String, Object> parseMap(String data) {
        if (data == null) {
            return new HashMap<String, Object>();
        }
        try {
            Map<String, Object> map = objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
            });
            return map;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private String writeMap(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private static AuthorizationGrantType resolveAuthorizationGrantType(String authorizationGrantType) {
        if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.AUTHORIZATION_CODE;
        } else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.CLIENT_CREDENTIALS;
        } else if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.REFRESH_TOKEN;
        }
        return new AuthorizationGrantType(authorizationGrantType);
    }
}
