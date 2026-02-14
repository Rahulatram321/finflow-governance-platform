package com.company.finflow.workflow.web;

import com.company.finflow.common.web.ApiResponse;
import com.company.finflow.common.web.PaginatedResponse;
import com.company.finflow.expense.domain.Expense;
import com.company.finflow.expense.repository.ExpenseRepository;
import com.company.finflow.tenant.domain.Tenant;
import com.company.finflow.tenant.context.TenantContext;
import com.company.finflow.tenant.repository.TenantRepository;
import com.company.finflow.user.domain.User;
import com.company.finflow.user.repository.UserRepository;
import com.company.finflow.workflow.domain.WorkflowStep;
import com.company.finflow.workflow.dto.WorkflowStepCreateDTO;
import com.company.finflow.workflow.dto.WorkflowStepResponseDTO;
import com.company.finflow.workflow.dto.WorkflowStepUpdateDTO;
import com.company.finflow.workflow.repository.WorkflowStepRepository;
import com.company.finflow.workflow.service.WorkflowService;
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

@Tag(name = "Workflow Steps")
@RestController
@RequestMapping("/api/workflow-steps")
@RequiredArgsConstructor
public class WorkflowStepController {

    private final WorkflowStepRepository workflowStepRepository;
    private final TenantRepository tenantRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final WorkflowService workflowService;

    @Operation(
        summary = "List workflow steps for tenant",
        description = "Supports pagination and sorting via query params: page, size, sort (e.g. sort=createdAt,desc)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<WorkflowStepResponseDTO>>> list(
        @Parameter(hidden = true)
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(PaginatedResponse.from(workflowService.listWorkflowSteps(pageable)))
        );
    }

    @Operation(summary = "Get workflow step by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowStepResponseDTO>> get(@PathVariable Long id) {
        Long tenantId = TenantContext.getRequiredTenantId();
        return ResponseEntity.ok(ApiResponse.success(toResponse(findWorkflowStep(id, tenantId))));
    }

    @Operation(summary = "Create workflow step")
    @PostMapping
    public ResponseEntity<ApiResponse<WorkflowStepResponseDTO>> create(@Valid @RequestBody WorkflowStepCreateDTO request) {
        Long tenantId = TenantContext.getRequiredTenantId();
        WorkflowStep workflowStep = new WorkflowStep();
        map(workflowStep, request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toResponse(workflowStepRepository.save(workflowStep))));
    }

    @Operation(summary = "Update workflow step")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowStepResponseDTO>> update(
        @PathVariable Long id,
        @Valid @RequestBody WorkflowStepUpdateDTO request
    ) {
        Long tenantId = TenantContext.getRequiredTenantId();
        WorkflowStep workflowStep = findWorkflowStep(id, tenantId);
        map(workflowStep, request, tenantId);
        return ResponseEntity.ok(ApiResponse.success(toResponse(workflowStepRepository.save(workflowStep))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long tenantId = TenantContext.getRequiredTenantId();
        WorkflowStep workflowStep = findWorkflowStep(id, tenantId);
        workflowStepRepository.delete(workflowStep);
        return ResponseEntity.ok(ApiResponse.success(null, "Workflow step deleted"));
    }

    private WorkflowStep findWorkflowStep(Long id, Long tenantId) {
        return workflowStepRepository.findByIdAndTenant_Id(id, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow step not found"));
    }

    private Tenant findTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
    }

    private Expense findExpenseInTenant(Long expenseId, Long tenantId) {
        return expenseRepository.findByIdAndTenant_Id(expenseId, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found for tenant"));
    }

    private User findUserInTenant(Long userId, Long tenantId) {
        return userRepository.findByIdAndTenant_Id(userId, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for tenant"));
    }

    private void map(WorkflowStep workflowStep, WorkflowStepCreateDTO request, Long tenantId) {
        workflowStep.setTenant(findTenant(tenantId));
        workflowStep.setExpense(findExpenseInTenant(request.getExpenseId(), tenantId));
        workflowStep.setAssignee(findUserInTenant(request.getAssigneeUserId(), tenantId));
        workflowStep.setStepOrder(request.getStepOrder());
        workflowStep.setStepType(request.getStepType().trim().toUpperCase());
        workflowStep.setStatus(request.getStatus());
        workflowStep.setActionedAt(request.getActionedAt());
        workflowStep.setComments(request.getComments());
    }

    private void map(WorkflowStep workflowStep, WorkflowStepUpdateDTO request, Long tenantId) {
        workflowStep.setTenant(findTenant(tenantId));
        workflowStep.setExpense(findExpenseInTenant(request.getExpenseId(), tenantId));
        workflowStep.setAssignee(findUserInTenant(request.getAssigneeUserId(), tenantId));
        workflowStep.setStepOrder(request.getStepOrder());
        workflowStep.setStepType(request.getStepType().trim().toUpperCase());
        workflowStep.setStatus(request.getStatus());
        workflowStep.setActionedAt(request.getActionedAt());
        workflowStep.setComments(request.getComments());
    }

    private WorkflowStepResponseDTO toResponse(WorkflowStep step) {
        Long assigneeId = step.getAssignee() == null ? null : step.getAssignee().getId();
        return WorkflowStepResponseDTO.builder()
            .id(step.getId())
            .tenantId(step.getTenant().getId())
            .expenseId(step.getExpense().getId())
            .stepOrder(step.getStepOrder())
            .stepType(step.getStepType())
            .status(step.getStatus())
            .assigneeUserId(assigneeId)
            .actionedAt(step.getActionedAt())
            .comments(step.getComments())
            .createdAt(step.getCreatedAt())
            .updatedAt(step.getUpdatedAt())
            .build();
    }
}
