package com.company.finflow.workflow.dto;

import com.company.finflow.workflow.domain.WorkflowStepStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WorkflowStepResponseDTO {
    Long id;
    Long tenantId;
    Long expenseId;
    Integer stepOrder;
    String stepType;
    WorkflowStepStatus status;
    Long assigneeUserId;
    Instant actionedAt;
    String comments;
    Instant createdAt;
    Instant updatedAt;
}
