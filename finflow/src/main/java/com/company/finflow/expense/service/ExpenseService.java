package com.company.finflow.expense.service;

import com.company.finflow.audit.service.AuditService;
import com.company.finflow.budget.service.BudgetService;
import com.company.finflow.expense.domain.Expense;
import com.company.finflow.expense.domain.ExpenseStatus;
import com.company.finflow.expense.dto.ExpenseCreateDTO;
import com.company.finflow.expense.dto.ExpenseUpdateDTO;
import com.company.finflow.expense.dto.ExpenseWorkflowResponseDTO;
import com.company.finflow.expense.repository.ExpenseRepository;
import com.company.finflow.security.auth.ApplicationRole;
import com.company.finflow.security.auth.CurrentUserService;
import com.company.finflow.security.auth.FinflowUserPrincipal;
import com.company.finflow.tenant.domain.Tenant;
import com.company.finflow.tenant.context.TenantContext;
import com.company.finflow.tenant.repository.TenantRepository;
import com.company.finflow.user.domain.User;
import com.company.finflow.user.repository.UserRepository;
import com.company.finflow.workflow.domain.WorkflowStep;
import com.company.finflow.workflow.domain.WorkflowStepStatus;
import com.company.finflow.workflow.dto.WorkflowStepResponseDTO;
import com.company.finflow.workflow.repository.WorkflowStepRepository;
import com.company.finflow.workflow.service.WorkflowService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final WorkflowService workflowService;
    private final BudgetService budgetService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<ExpenseWorkflowResponseDTO> listExpenses(Pageable pageable) {
        Long tenantId = TenantContext.getRequiredTenantId();
        FinflowUserPrincipal currentUser = currentUserService.getCurrentUser();

        Page<Expense> expenses = currentUser.hasRole(ApplicationRole.EMPLOYEE)
            ? expenseRepository.findAllByTenant_IdAndSubmittedBy_Id(tenantId, currentUser.getUserId(), pageable)
            : expenseRepository.findAllByTenant_Id(tenantId, pageable);

        return expenses.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ExpenseWorkflowResponseDTO getExpense(Long expenseId) {
        Long tenantId = TenantContext.getRequiredTenantId();
        FinflowUserPrincipal currentUser = currentUserService.getCurrentUser();

        Expense expense = currentUser.hasRole(ApplicationRole.EMPLOYEE)
            ? expenseRepository.findByIdAndTenant_IdAndSubmittedBy_Id(expenseId, tenantId, currentUser.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"))
            : expenseRepository.findByIdAndTenant_Id(expenseId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));

        return toResponse(expense);
    }

    @Transactional
    public ExpenseWorkflowResponseDTO createExpense(ExpenseCreateDTO request) {
        Long tenantId = TenantContext.getRequiredTenantId();
        FinflowUserPrincipal currentUser = currentUserService.getCurrentUser();

        Tenant tenant = findTenant(tenantId);
        User actor = findUserInTenant(currentUser.getUserId(), tenantId);

        Expense expense = new Expense();
        expense.setTenant(tenant);
        expense.setSubmittedBy(actor);
        expense.setApprovedBy(null);
        applyUpdatableFields(expense, request);
        expense.setStatus(ExpenseStatus.DRAFT);

        expenseRepository.save(expense);
        auditService.writeAudit(
            tenantId,
            currentUser.getUserId(),
            "EXPENSE",
            expense.getId(),
            "EXPENSE_CREATED",
            null,
            expenseSnapshot(expense)
        );

        return toResponse(expense);
    }

    @Transactional
    public ExpenseWorkflowResponseDTO updateExpense(Long expenseId, ExpenseUpdateDTO request) {
        Long tenantId = TenantContext.getRequiredTenantId();
        FinflowUserPrincipal currentUser = currentUserService.getCurrentUser();

        Expense expense = findExpenseForUpdate(expenseId, tenantId);
        ensureEditable(expense);
        ensureEmployeeOwnership(expense, currentUser);

        Map<String, Object> oldValue = expenseSnapshot(expense);
        applyUpdatableFields(expense, request);

        expenseRepository.save(expense);
        auditService.writeAudit(
            tenantId,
            currentUser.getUserId(),
            "EXPENSE",
            expense.getId(),
            "EXPENSE_UPDATED",
            oldValue,
            expenseSnapshot(expense)
        );

        return toResponse(expense);
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        Long tenantId = TenantContext.getRequiredTenantId();
        FinflowUserPrincipal currentUser = currentUserService.getCurrentUser();

        Expense expense = findExpenseForUpdate(expenseId, tenantId);
        ensureEditable(expense);
        ensureEmployeeOwnership(expense, currentUser);
        Map<String, Object> oldValue = expenseSnapshot(expense);

        expenseRepository.delete(expense);
        auditService.writeAudit(
            tenantId,
            currentUser.getUserId(),
            "EXPENSE",
            expenseId,
            "EXPENSE_DELETED",
            oldValue,
            null
        );
    }

    @Transactional
    public ExpenseWorkflowResponseDTO submitExpense(Long expenseId) {
        Long tenantId = TenantContext.getRequiredTenantId();
        FinflowUserPrincipal currentUser = currentUserService.getCurrentUser();

        Expense expense = findExpenseForUpdate(expenseId, tenantId);
        ensureEmployeeOwnership(expense, currentUser);
        if (expense.getStatus() != ExpenseStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only DRAFT expenses can be submitted");
        }

        Map<String, Object> oldValue = expenseSnapshot(expense);
        expense.setStatus(ExpenseStatus.SUBMITTED);
        workflowService.createDefaultApprovalWorkflow(expense);

        expenseRepository.save(expense);
        auditService.writeAudit(
            tenantId,
            currentUser.getUserId(),
            "EXPENSE",
            expense.getId(),
            "EXPENSE_SUBMITTED",
            oldValue,
            expenseSnapshot(expense)
        );

        return toResponse(expense);
    }

    @Transactional
    public ExpenseWorkflowResponseDTO approveExpenseStep(Long workflowStepId) {
        Long tenantId = TenantContext.getRequiredTenantId();
        FinflowUserPrincipal currentUser = currentUserService.getCurrentUser();

        WorkflowStep step = workflowService.findStepForUpdate(workflowStepId);
        enforceStepAssignee(step, currentUser.getUserId());
        enforcePendingStep(step);
        enforceStepSequence(step);

        Expense expense = findExpenseForUpdate(step.getExpense().getId(), tenantId);
        Map<String, Object> oldValue = workflowActionSnapshot(expense, step);
        String auditAction = "WORKFLOW_STEP_APPROVED";

        if (workflowService.isManagerApprovalStep(step)) {
            enforceExpenseStatus(expense, ExpenseStatus.SUBMITTED, "Manager step approval");
            step.setStatus(WorkflowStepStatus.APPROVED);
            step.setActionedAt(Instant.now());
            expense.setStatus(ExpenseStatus.MANAGER_APPROVED);
        } else if (workflowService.isFinanceApprovalStep(step)) {
            enforceExpenseStatus(expense, ExpenseStatus.MANAGER_APPROVED, "Finance step approval");
            budgetService.validateBudgetBeforeFinalApproval(expense);
            step.setStatus(WorkflowStepStatus.APPROVED);
            step.setActionedAt(Instant.now());
            expense.setStatus(ExpenseStatus.FINANCE_APPROVED);
            expense.setApprovedBy(findUserInTenant(currentUser.getUserId(), tenantId));
            auditAction = "EXPENSE_READY_FOR_PAYMENT";
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Unsupported workflow step type: " + step.getStepType());
        }

        workflowStepRepository.save(step);
        expenseRepository.save(expense);
        auditService.writeAudit(
            tenantId,
            currentUser.getUserId(),
            "EXPENSE",
            expense.getId(),
            auditAction,
            oldValue,
            workflowActionSnapshot(expense, step)
        );

        return toResponse(expense);
    }

    @Transactional
    public ExpenseWorkflowResponseDTO rejectExpenseStep(Long workflowStepId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejection reason is required");
        }

        Long tenantId = TenantContext.getRequiredTenantId();
        FinflowUserPrincipal currentUser = currentUserService.getCurrentUser();

        WorkflowStep step = workflowService.findStepForUpdate(workflowStepId);
        enforceStepAssignee(step, currentUser.getUserId());
        enforcePendingStep(step);
        enforceStepSequence(step);

        Expense expense = findExpenseForUpdate(step.getExpense().getId(), tenantId);
        if (expense.getStatus() == ExpenseStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Paid expense cannot be rejected");
        }

        Map<String, Object> oldValue = workflowActionSnapshot(expense, step);
        step.setStatus(WorkflowStepStatus.REJECTED);
        step.setActionedAt(Instant.now());
        step.setComments(reason.trim());
        expense.setStatus(ExpenseStatus.REJECTED);
        workflowService.cancelPendingSteps(expense.getId(), step.getId(), "Rejected: " + reason.trim());

        workflowStepRepository.save(step);
        expenseRepository.save(expense);
        auditService.writeAudit(
            tenantId,
            currentUser.getUserId(),
            "EXPENSE",
            expense.getId(),
            "WORKFLOW_STEP_REJECTED",
            oldValue,
            workflowActionSnapshot(expense, step)
        );

        return toResponse(expense);
    }

    private Expense findExpenseForUpdate(Long expenseId, Long tenantId) {
        return expenseRepository.findByIdAndTenantIdForUpdate(expenseId, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
    }

    private Tenant findTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
    }

    private User findUserInTenant(Long userId, Long tenantId) {
        return userRepository.findByIdAndTenant_Id(userId, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for tenant"));
    }

    private void enforceStepAssignee(WorkflowStep step, Long userId) {
        if (step.getAssignee() == null || !step.getAssignee().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assigned approver can act on this step");
        }
    }

    private void enforcePendingStep(WorkflowStep step) {
        if (step.getStatus() != WorkflowStepStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Workflow step is already processed");
        }
    }

    private void enforceStepSequence(WorkflowStep currentStep) {
        List<WorkflowStep> steps = workflowService.getWorkflowForExpense(currentStep.getExpense().getId());
        boolean pendingEarlierStepExists = steps.stream()
            .anyMatch(step ->
                step.getStepOrder() < currentStep.getStepOrder() && step.getStatus() == WorkflowStepStatus.PENDING
            );
        if (pendingEarlierStepExists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Previous workflow step must be completed before this action"
            );
        }
    }

    private void ensureEditable(Expense expense) {
        if (expense.getStatus() != ExpenseStatus.DRAFT && expense.getStatus() != ExpenseStatus.REJECTED) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Expense can only be edited or deleted in DRAFT or REJECTED state"
            );
        }
    }

    private void ensureEmployeeOwnership(Expense expense, FinflowUserPrincipal currentUser) {
        if (currentUser.hasRole(ApplicationRole.EMPLOYEE)
            && !expense.getSubmittedBy().getId().equals(currentUser.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Employees can only manage their own expenses");
        }
    }

    private void enforceExpenseStatus(Expense expense, ExpenseStatus expected, String action) {
        if (expense.getStatus() != expected) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                action + " is not allowed when expense is in " + expense.getStatus() + " state"
            );
        }
    }

    private void applyUpdatableFields(Expense expense, ExpenseCreateDTO request) {
        expense.setTitle(request.getTitle().trim());
        expense.setDescription(request.getDescription());
        expense.setCategory(request.getCategory().trim());
        expense.setAmount(request.getAmount());
        expense.setCurrencyCode(request.getCurrencyCode().trim().toUpperCase());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setReceiptUrl(request.getReceiptUrl());
    }

    private void applyUpdatableFields(Expense expense, ExpenseUpdateDTO request) {
        expense.setTitle(request.getTitle().trim());
        expense.setDescription(request.getDescription());
        expense.setCategory(request.getCategory().trim());
        expense.setAmount(request.getAmount());
        expense.setCurrencyCode(request.getCurrencyCode().trim().toUpperCase());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setReceiptUrl(request.getReceiptUrl());
    }

    private ExpenseWorkflowResponseDTO toResponse(Expense expense) {
        List<WorkflowStepResponseDTO> workflowSteps = workflowService
            .getWorkflowForExpense(expense.getId())
            .stream()
            .map(this::toStepResponse)
            .toList();

        return ExpenseWorkflowResponseDTO.builder()
            .id(expense.getId())
            .status(expense.getStatus())
            .workflowSteps(workflowSteps)
            .build();
    }

    private WorkflowStepResponseDTO toStepResponse(WorkflowStep step) {
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

    private Map<String, Object> expenseSnapshot(Expense expense) {
        return Map.of(
            "id", expense.getId(),
            "status", expense.getStatus().name()
        );
    }

    private Map<String, Object> workflowActionSnapshot(Expense expense, WorkflowStep step) {
        Long assigneeId = step.getAssignee() == null ? null : step.getAssignee().getId();
        return Map.of(
            "expenseId", expense.getId(),
            "expenseStatus", expense.getStatus().name(),
            "workflowStepId", step.getId(),
            "workflowStepStatus", step.getStatus().name(),
            "workflowStepType", step.getStepType(),
            "assigneeUserId", assigneeId
        );
    }
}
