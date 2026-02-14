package com.company.finflow.security.jwt;

import com.company.finflow.security.auth.ApplicationRole;
import com.company.finflow.security.auth.FinflowUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JwtService {

    private final String jwtSecret;
    private final long expirationMs;

    public JwtService(
        @Value("${jwt.secret}") String jwtSecret,
        @Value("${jwt.expiration-ms:3600000}") long expirationMs
    ) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured");
        }
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters");
        }
        this.jwtSecret = jwtSecret;
        this.expirationMs = expirationMs;
    }

    public String generateToken(FinflowUserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);
        return Jwts.builder()
            .setSubject(principal.getUsername())
            .claim("userId", principal.getUserId())
            .claim("tenantId", principal.getTenantId())
            .claim("role", principal.getRole().name())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiry))
            .signWith(signingKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public JwtTokenClaims parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

            Long userId = claims.get("userId", Long.class);
            Long tenantId = claims.get("tenantId", Long.class);
            String role = claims.get("role", String.class);
            String email = claims.getSubject();
            Instant issuedAt = claims.getIssuedAt().toInstant();
            Instant expiresAt = claims.getExpiration().toInstant();

            if (userId == null || tenantId == null || role == null || email == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication token");
            }

            return new JwtTokenClaims(
                userId,
                tenantId,
                email,
                ApplicationRole.valueOf(role),
                issuedAt,
                expiresAt
            );
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired authentication token");
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
