package com.company.finflow.audit.service;

import com.company.finflow.audit.domain.AuditLog;
import com.company.finflow.audit.dto.AuditLogResponseDTO;
import com.company.finflow.audit.repository.AuditLogRepository;
import com.company.finflow.tenant.context.TenantContext;
import com.company.finflow.tenant.domain.Tenant;
import com.company.finflow.tenant.repository.TenantRepository;
import com.company.finflow.user.domain.User;
import com.company.finflow.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> listAuditLogs(Pageable pageable) {
        Long tenantId = TenantContext.getRequiredTenantId();
        return auditLogRepository.findAllByTenant_Id(tenantId, pageable).map(this::toResponse);
    }

    @Transactional
    public void writeAudit(
        Long tenantId,
        Long userId,
        String entityType,
        Long entityId,
        String action,
        Object oldValue,
        Object newValue
    ) {
        Long contextTenantId = TenantContext.getRequiredTenantId();
        if (tenantId != null && !tenantId.equals(contextTenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant context mismatch for audit operation");
        }

        Tenant tenant = tenantRepository.findById(contextTenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));

        User actor = null;
        if (userId != null) {
            actor = userRepository.findByIdAndTenant_Id(userId, contextTenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for tenant"));
        }

        AuditLog auditLog = new AuditLog();
        auditLog.setTenant(tenant);
        auditLog.setUser(actor);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setOldValues(toJson(oldValue));
        auditLog.setNewValues(toJson(newValue));

        auditLogRepository.save(auditLog);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize audit payload");
        }
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
