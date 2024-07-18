package com.vapps.auth.dto;

import lombok.Data;

@Data
public class ScopeDTO {
    
    private String scopeId;
    private String scopeName;
    private ClientDTO client;

}
