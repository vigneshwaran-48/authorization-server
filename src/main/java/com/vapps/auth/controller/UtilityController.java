package com.vapps.auth.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vapps.auth.exception.AppException;

@RestController
@RequestMapping("/api/utility")
public class UtilityController {
    
    @GetMapping("/get-routes")
    public ResponseEntity<?> getAppRoutes(Principal principal)
            throws AppException {

        if(principal == null || principal.getName() == null) {
            throw new AppException(401, "UserDetails not found");
        }

        String userId = principal.getName();

        Map<String, Object> user = new HashMap<>();
        String userBase = "/api/user";
        user.put("base", userBase);
        user.put("getUser", userBase + "/me");
        user.put("profileImage", userBase + "/" + userId + "/profile-image");

        Map<String, Object> client = new HashMap<>();
        String clientBase = userBase + "/" + userId + "/client";
        client.put("base", clientBase);

        Map<String, Object> oauth = new HashMap<>();
        String oauthBase = "/api/oauth";
        oauth.put("base", oauthBase);
        oauth.put("consent", oauthBase + "/consent");

        Routes routes = new Routes(user, client, oauth);
        return ResponseEntity.ok(routes);
    }

    record Routes(Map<String, Object> user, Map<String, Object> client, Map<String, Object> oauth) {}

}
