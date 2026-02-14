package com.company.finflow.expense.dto;

import com.company.finflow.expense.domain.ExpenseStatus;
import com.company.finflow.workflow.dto.WorkflowStepResponseDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExpenseWorkflowResponseDTO {
    Long id;
    ExpenseStatus status;
    @Builder.Default
    List<WorkflowStepResponseDTO> workflowSteps = new ArrayList<>();
}
