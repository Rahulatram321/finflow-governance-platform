package com.company.finflow.audit.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuditLogResponseDTO {
    Long id;
    Long tenantId;
    Long userId;
    String entityType;
    Long entityId;
    String action;
    String oldValues;
    String newValues;
    String ipAddress;
    Instant createdAt;
    Instant updatedAt;
}
