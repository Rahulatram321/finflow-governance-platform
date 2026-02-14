package com.company.finflow.user.web;

import com.company.finflow.common.web.ApiResponse;
import com.company.finflow.common.web.PaginatedResponse;
import com.company.finflow.tenant.domain.Tenant;
import com.company.finflow.tenant.context.TenantContext;
import com.company.finflow.tenant.repository.TenantRepository;
import com.company.finflow.user.domain.User;
import com.company.finflow.user.dto.UserCreateDTO;
import com.company.finflow.user.dto.UserResponseDTO;
import com.company.finflow.user.dto.UserUpdateDTO;
import com.company.finflow.user.repository.UserRepository;
import com.company.finflow.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "User")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Operation(
        summary = "List users in tenant",
        description = "Supports pagination and sorting via query params: page, size, sort (e.g. sort=createdAt,desc)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponseDTO>>> list(
        @Parameter(hidden = true)
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.from(userService.listUsers(pageable))));
    }

    @Operation(summary = "Get user by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> get(@PathVariable Long id) {
        Long tenantId = TenantContext.getRequiredTenantId();
        return ResponseEntity.ok(ApiResponse.success(toResponse(findUser(id, tenantId))));
    }

    @Operation(summary = "Create user")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> create(@Valid @RequestBody UserCreateDTO request) {
        Long tenantId = TenantContext.getRequiredTenantId();
        Tenant tenant = findTenant(tenantId);
        User user = new User();
        user.setTenant(tenant);
        apply(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toResponse(userRepository.save(user))));
    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO request) {
        Long tenantId = TenantContext.getRequiredTenantId();
        User user = findUser(id, tenantId);
        user.setTenant(findTenant(tenantId));
        apply(user, request);
        return ResponseEntity.ok(ApiResponse.success(toResponse(userRepository.save(user))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long tenantId = TenantContext.getRequiredTenantId();
        User user = findUser(id, tenantId);
        userRepository.delete(user);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted"));
    }

    private Tenant findTenant(Long id) {
        return tenantRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
    }

    private User findUser(Long id, Long tenantId) {
        return userRepository.findByIdAndTenant_Id(id, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void apply(User user, UserCreateDTO request) {
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPasswordHash().trim()));
        user.setRole(request.getRole().trim().toUpperCase());
        user.setStatus(request.getStatus().trim().toUpperCase());
    }

    private void apply(User user, UserUpdateDTO request) {
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPasswordHash().trim()));
        user.setRole(request.getRole().trim().toUpperCase());
        user.setStatus(request.getStatus().trim().toUpperCase());
    }

    private UserResponseDTO toResponse(User user) {
        return UserResponseDTO.builder()
            .id(user.getId())
            .tenantId(user.getTenant().getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole())
            .status(user.getStatus())
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
