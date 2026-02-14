package com.company.finflow.security.service;

import com.company.finflow.security.auth.FinflowUserDetailsService;
import com.company.finflow.security.auth.FinflowUserPrincipal;
import com.company.finflow.security.dto.AuthLoginRequestDTO;
import com.company.finflow.security.dto.AuthTokenResponseDTO;
import com.company.finflow.security.jwt.JwtService;
import com.company.finflow.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final FinflowUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public AuthTokenResponseDTO login(AuthLoginRequestDTO request, Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid tenant id");
        }

        FinflowUserPrincipal user = userDetailsService.loadByTenantAndEmail(tenantId, request.getEmail());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive");
        }

        String token = jwtService.generateToken(user);
        return AuthTokenResponseDTO.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresInSeconds(jwtService.getExpirationMs() / 1000)
            .userId(user.getUserId())
            .tenantId(user.getTenantId())
            .role(user.getRole().name())
            .build();
    }
}
