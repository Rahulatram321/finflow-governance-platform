package com.company.finflow.workflow.service;

import com.company.finflow.expense.domain.Expense;
import com.company.finflow.user.domain.User;
import com.company.finflow.user.repository.UserRepository;
import com.company.finflow.tenant.context.TenantContext;
import com.company.finflow.workflow.domain.WorkflowStep;
import com.company.finflow.workflow.domain.WorkflowStepStatus;
import com.company.finflow.workflow.dto.WorkflowStepResponseDTO;
import com.company.finflow.workflow.repository.WorkflowStepRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    public static final String MANAGER_APPROVAL = "MANAGER_APPROVAL";
    public static final String FINANCE_APPROVAL = "FINANCE_APPROVAL";

    private static final String ACTIVE_USER_STATUS = "ACTIVE";

    private final WorkflowStepRepository workflowStepRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createDefaultApprovalWorkflow(Expense expense) {
        Long tenantId = currentTenantId();
        if (!tenantId.equals(expense.getTenant().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant mismatch for workflow creation");
        }
        if (workflowStepRepository.existsByExpense_IdAndTenant_Id(expense.getId(), tenantId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Workflow has already been created for this expense"
            );
        }

        User managerApprover = resolveApprover(tenantId, "ADMIN");
        User financeApprover = resolveApprover(tenantId, "FINANCE_MANAGER");

        WorkflowStep managerStep = new WorkflowStep();
        managerStep.setTenant(expense.getTenant());
        managerStep.setExpense(expense);
        managerStep.setStepOrder(1);
        managerStep.setStepType(MANAGER_APPROVAL);
        managerStep.setAssignee(managerApprover);
        managerStep.setStatus(WorkflowStepStatus.PENDING);

        WorkflowStep financeStep = new WorkflowStep();
        financeStep.setTenant(expense.getTenant());
        financeStep.setExpense(expense);
        financeStep.setStepOrder(2);
        financeStep.setStepType(FINANCE_APPROVAL);
        financeStep.setAssignee(financeApprover);
        financeStep.setStatus(WorkflowStepStatus.PENDING);

        workflowStepRepository.saveAll(List.of(managerStep, financeStep));
    }

    @Transactional(readOnly = true)
    public Page<WorkflowStepResponseDTO> listWorkflowSteps(Pageable pageable) {
        Long tenantId = currentTenantId();
        return workflowStepRepository.findAllByTenant_Id(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<WorkflowStep> getWorkflowForExpense(Long expenseId) {
        Long tenantId = currentTenantId();
        return workflowStepRepository.findAllByExpense_IdAndTenant_IdOrderByStepOrderAsc(expenseId, tenantId);
    }

    @Transactional
    public WorkflowStep findStepForUpdate(Long stepId) {
        Long tenantId = currentTenantId();
        return workflowStepRepository.findByIdAndTenantIdForUpdate(stepId, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow step not found"));
    }

    @Transactional
    public void cancelPendingSteps(Long expenseId, Long exceptStepId, String reason) {
        Long tenantId = currentTenantId();
        List<WorkflowStep> pendingSteps = workflowStepRepository
            .findAllByExpense_IdAndTenant_IdAndStatusOrderByStepOrderAsc(expenseId, tenantId, WorkflowStepStatus.PENDING);

        List<WorkflowStep> toSkip = new ArrayList<>();
        for (WorkflowStep step : pendingSteps) {
            if (step.getId().equals(exceptStepId)) {
                continue;
            }
            step.setStatus(WorkflowStepStatus.SKIPPED);
            step.setActionedAt(Instant.now());
            step.setComments(reason);
            toSkip.add(step);
        }

        if (!toSkip.isEmpty()) {
            workflowStepRepository.saveAll(toSkip);
        }
    }

    public boolean isManagerApprovalStep(WorkflowStep step) {
        return MANAGER_APPROVAL.equals(step.getStepType());
    }

    public boolean isFinanceApprovalStep(WorkflowStep step) {
        return FINANCE_APPROVAL.equals(step.getStepType());
    }

    private Long currentTenantId() {
        return TenantContext.getRequiredTenantId();
    }

    private User resolveApprover(Long tenantId, String role) {
        return userRepository.findFirstByTenant_IdAndRoleIgnoreCaseAndStatusIgnoreCase(tenantId, role, ACTIVE_USER_STATUS)
            .or(() -> userRepository.findFirstByTenant_IdAndRoleIgnoreCase(tenantId, role))
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No " + role.toLowerCase() + " approver configured for tenant"
            ));
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
