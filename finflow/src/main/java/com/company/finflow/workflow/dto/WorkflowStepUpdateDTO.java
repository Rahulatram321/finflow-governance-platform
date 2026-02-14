package com.company.finflow.workflow.dto;

import com.company.finflow.workflow.domain.WorkflowStepStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkflowStepUpdateDTO {

    @NotNull
    private Long expenseId;

    @NotNull
    private Long assigneeUserId;

    @NotNull
    @Min(1)
    private Integer stepOrder;

    @NotBlank
    @Size(max = 60)
    private String stepType;

    @NotNull
    private WorkflowStepStatus status;

    private Instant actionedAt;

    @Size(max = 4000)
    private String comments;
}
