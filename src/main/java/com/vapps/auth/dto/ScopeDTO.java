package com.vapps.auth.dto;

import lombok.Data;

@Data
public class ScopeDTO {
    
    private Long scopeId;
    private String scopeName;
    private ClientDTO client;

}
