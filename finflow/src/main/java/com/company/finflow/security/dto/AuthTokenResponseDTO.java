package com.company.finflow.security.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthTokenResponseDTO {
    String accessToken;
    String tokenType;
    long expiresInSeconds;
    Long userId;
    Long tenantId;
    String role;
}
