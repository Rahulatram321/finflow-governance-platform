package com.company.finflow.audit.web;

import com.company.finflow.audit.domain.AuditLog;
import com.company.finflow.audit.dto.AuditLogCreateDTO;
import com.company.finflow.audit.dto.AuditLogResponseDTO;
import com.company.finflow.audit.dto.AuditLogUpdateDTO;
import com.company.finflow.audit.repository.AuditLogRepository;
import com.company.finflow.audit.service.AuditService;
import com.company.finflow.common.web.ApiResponse;
import com.company.finflow.common.web.PaginatedResponse;
import com.company.finflow.tenant.domain.Tenant;
import com.company.finflow.tenant.context.TenantContext;
import com.company.finflow.tenant.repository.TenantRepository;
import com.company.finflow.user.domain.User;
import com.company.finflow.user.repository.UserRepository;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Audit")
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Operation(
        summary = "List audit logs",
        description = "Supports pagination and sorting via query params: page, size, sort (e.g. sort=createdAt,desc)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLogResponseDTO>>> list(
        @Parameter(hidden = true)
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.from(auditService.listAuditLogs(pageable))));
    }

    @Operation(summary = "Get audit log by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponseDTO>> get(@PathVariable Long id) {
        Long tenantId = TenantContext.getRequiredTenantId();
        return ResponseEntity.ok(ApiResponse.success(toResponse(findAuditLog(id, tenantId))));
    }

    @Operation(summary = "Create audit log")
    @PostMapping
    public ResponseEntity<ApiResponse<AuditLogResponseDTO>> create(@Valid @RequestBody AuditLogCreateDTO request) {
        Long tenantId = TenantContext.getRequiredTenantId();
        AuditLog auditLog = new AuditLog();
        map(auditLog, request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toResponse(auditLogRepository.save(auditLog))));
    }

    @Operation(summary = "Update audit log")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponseDTO>> update(@PathVariable Long id, @Valid @RequestBody AuditLogUpdateDTO request) {
        Long tenantId = TenantContext.getRequiredTenantId();
        AuditLog auditLog = findAuditLog(id, tenantId);
        map(auditLog, request, tenantId);
        return ResponseEntity.ok(ApiResponse.success(toResponse(auditLogRepository.save(auditLog))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long tenantId = TenantContext.getRequiredTenantId();
        AuditLog auditLog = findAuditLog(id, tenantId);
        auditLogRepository.delete(auditLog);
        return ResponseEntity.ok(ApiResponse.success(null, "Audit log deleted"));
    }

    private AuditLog findAuditLog(Long id, Long tenantId) {
        return auditLogRepository.findByIdAndTenant_Id(id, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Audit log not found"));
    }

    private Tenant findTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
    }

    private User findUserInTenant(Long userId, Long tenantId) {
        return userRepository.findByIdAndTenant_Id(userId, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for tenant"));
    }

    private void map(AuditLog auditLog, AuditLogCreateDTO request, Long tenantId) {
        auditLog.setTenant(findTenant(tenantId));
        if (request.getUserId() == null) {
            auditLog.setUser(null);
        } else {
            auditLog.setUser(findUserInTenant(request.getUserId(), tenantId));
        }
        auditLog.setEntityType(request.getEntityType().trim().toUpperCase());
        auditLog.setEntityId(request.getEntityId());
        auditLog.setAction(request.getAction().trim().toUpperCase());
        auditLog.setOldValues(request.getOldValues());
        auditLog.setNewValues(request.getNewValues());
        auditLog.setIpAddress(request.getIpAddress());
    }

    private void map(AuditLog auditLog, AuditLogUpdateDTO request, Long tenantId) {
        auditLog.setTenant(findTenant(tenantId));
        if (request.getUserId() == null) {
            auditLog.setUser(null);
        } else {
            auditLog.setUser(findUserInTenant(request.getUserId(), tenantId));
        }
        auditLog.setEntityType(request.getEntityType().trim().toUpperCase());
        auditLog.setEntityId(request.getEntityId());
        auditLog.setAction(request.getAction().trim().toUpperCase());
        auditLog.setOldValues(request.getOldValues());
        auditLog.setNewValues(request.getNewValues());
        auditLog.setIpAddress(request.getIpAddress());
    }

    private AuditLogResponseDTO toResponse(AuditLog auditLog) {
        Long userId = auditLog.getUser() == null ? null : auditLog.getUser().getId();
        return AuditLogResponseDTO.builder()
            .id(auditLog.getId())
            .tenantId(auditLog.getTenant().getId())
            .userId(userId)
            .entityType(auditLog.getEntityType())
            .entityId(auditLog.getEntityId())
            .action(auditLog.getAction())
            .oldValues(auditLog.getOldValues())
            .newValues(auditLog.getNewValues())
            .ipAddress(auditLog.getIpAddress())
            .createdAt(auditLog.getCreatedAt())
            .updatedAt(auditLog.getUpdatedAt())
            .build();
    }
}
