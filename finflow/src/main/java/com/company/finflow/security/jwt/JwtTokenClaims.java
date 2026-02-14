package com.company.finflow.security.jwt;

import com.company.finflow.security.auth.ApplicationRole;
import java.time.Instant;

public record JwtTokenClaims(
    Long userId,
    Long tenantId,
    String email,
    ApplicationRole role,
    Instant issuedAt,
    Instant expiresAt
) {
}
