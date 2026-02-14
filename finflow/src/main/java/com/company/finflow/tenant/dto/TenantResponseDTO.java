package com.company.finflow.tenant.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TenantResponseDTO {
    Long id;
    String code;
    String name;
    String status;
    String currencyCode;
    Instant createdAt;
    Instant updatedAt;
}
