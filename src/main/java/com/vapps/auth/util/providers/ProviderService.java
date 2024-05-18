package com.vapps.auth.util.providers;

import org.springframework.security.oauth2.core.user.OAuth2User;

import com.vapps.auth.model.AppUser;

public abstract class ProviderService {

    protected OAuth2User oAuth2User;

    public void setOAuth2User(OAuth2User oAuth2User) {
        this.oAuth2User = oAuth2User;
    }

    protected void setPasswordForExternalServiceUser(AppUser appUser) {
        appUser.setPassword("****(Other service account)");
    }

    public abstract String getId();

    public abstract AppUser buildAppUser();
}
