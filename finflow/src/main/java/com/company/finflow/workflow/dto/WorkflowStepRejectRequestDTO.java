package com.company.finflow.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkflowStepRejectRequestDTO {

    @NotBlank
    @Size(max = 500)
    private String reason;
}
