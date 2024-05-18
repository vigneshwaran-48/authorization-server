package com.vapps.auth.util;

import org.springframework.security.oauth2.core.user.OAuth2User;

import com.vapps.auth.util.providers.GithubProviderService;
import com.vapps.auth.util.providers.GoogleProviderService;
import com.vapps.auth.util.providers.ProviderService;

public enum OAuth2Provider {
    GOOGLE("google", new GoogleProviderService()),
    GITHUB("github", new GithubProviderService()),
    VAPPS("vapps", null);

    private String providerName;
    private ProviderService providerService;

    OAuth2Provider(String providerName, ProviderService providerService) {
        this.providerName = providerName;
        this.providerService = providerService;
    }

    public String getProviderName() {
        return providerName;
    }

    public ProviderService getProviderService(OAuth2User oAuth2User) {
        providerService.setOAuth2User(oAuth2User);
        return providerService;
    }

    public static OAuth2Provider getByProviderName(String providerName) {
        for (OAuth2Provider provider : values()) {
            if (provider.getProviderName().equals(providerName)) {
                return provider;
            }
        }
        return null;
    }
}