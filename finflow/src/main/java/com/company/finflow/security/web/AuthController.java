package com.company.finflow.security.web;

import com.company.finflow.common.web.ApiResponse;
import com.company.finflow.security.dto.AuthLoginRequestDTO;
import com.company.finflow.security.dto.AuthTokenResponseDTO;
import com.company.finflow.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Authenticate user and return JWT token")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenResponseDTO>> login(
        @RequestHeader("X-Tenant-Id") Long tenantId,
        @Valid @RequestBody AuthLoginRequestDTO request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request, tenantId)));
    }
}
