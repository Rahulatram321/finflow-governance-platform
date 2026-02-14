package com.company.finflow.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuditLogUpdateDTO {

    private Long userId;

    @NotBlank
    @Size(max = 80)
    private String entityType;

    @NotNull
    private Long entityId;

    @NotBlank
    @Size(max = 80)
    private String action;

    private String oldValues;

    private String newValues;

    @Size(max = 64)
    private String ipAddress;
}
