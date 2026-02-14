package com.company.finflow.tenant.web;

import com.company.finflow.common.web.ApiResponse;
import com.company.finflow.tenant.domain.Tenant;
import com.company.finflow.tenant.dto.TenantCreateDTO;
import com.company.finflow.tenant.dto.TenantResponseDTO;
import com.company.finflow.tenant.dto.TenantUpdateDTO;
import com.company.finflow.tenant.repository.TenantRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Tenant")
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantRepository tenantRepository;

    @Operation(summary = "List tenants")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantResponseDTO>>> list() {
        List<TenantResponseDTO> data = tenantRepository.findAll().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "Get tenant by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponseDTO>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(toResponse(findTenant(id))));
    }

    @Operation(summary = "Create tenant")
    @PostMapping
    public ResponseEntity<ApiResponse<TenantResponseDTO>> create(@Valid @RequestBody TenantCreateDTO request) {
        Tenant tenant = new Tenant();
        apply(tenant, request.getCode(), request.getName(), request.getStatus(), request.getCurrencyCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toResponse(tenantRepository.save(tenant))));
    }

    @Operation(summary = "Update tenant")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponseDTO>> update(@PathVariable Long id, @Valid @RequestBody TenantUpdateDTO request) {
        Tenant tenant = findTenant(id);
        apply(tenant, request.getCode(), request.getName(), request.getStatus(), request.getCurrencyCode());
        return ResponseEntity.ok(ApiResponse.success(toResponse(tenantRepository.save(tenant))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Tenant tenant = findTenant(id);
        tenantRepository.delete(tenant);
        return ResponseEntity.ok(ApiResponse.success(null, "Tenant deleted"));
    }

    private Tenant findTenant(Long id) {
        return tenantRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
    }

    private void apply(Tenant tenant, String code, String name, String status, String currencyCode) {
        tenant.setCode(code.trim());
        tenant.setName(name.trim());
        tenant.setStatus(status.trim());
        tenant.setCurrencyCode(currencyCode.trim().toUpperCase());
    }

    private TenantResponseDTO toResponse(Tenant tenant) {
        return TenantResponseDTO.builder()
            .id(tenant.getId())
            .code(tenant.getCode())
            .name(tenant.getName())
            .status(tenant.getStatus())
            .currencyCode(tenant.getCurrencyCode())
            .createdAt(tenant.getCreatedAt())
            .updatedAt(tenant.getUpdatedAt())
            .build();
    }
}
