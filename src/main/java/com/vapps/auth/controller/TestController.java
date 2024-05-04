package com.vapps.auth.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    @GetMapping
    public String getAccessToken() {
        OAuth2AuthenticationToken oauth2Token =
                (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        LOGGER.info(oauth2Token.getPrincipal().toString());
        return "Hi";
    }
}
