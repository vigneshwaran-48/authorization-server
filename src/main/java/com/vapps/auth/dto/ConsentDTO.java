package com.vapps.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConsentDTO {

    @Data
    public static class ScopeDetails {
        private String name;
        private String description;
        private boolean isAlreadyAllowed;
    }

    @Data
    public static class ClientScopeDetails {
        private String clientId;
        private String clientName;
        private List<ScopeDetails> scopeDetails;
    }

    private String clientId;
    private String clientName;
    private String userEmail;
    private String userName;
    private List<ClientScopeDetails> clientScopes;

}
