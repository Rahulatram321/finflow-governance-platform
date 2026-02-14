package com.company.finflow.budget.service;

import com.company.finflow.budget.domain.Budget;
import com.company.finflow.budget.dto.BudgetResponseDTO;
import com.company.finflow.budget.repository.BudgetRepository;
import com.company.finflow.expense.domain.Expense;
import com.company.finflow.tenant.context.TenantContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private static final BigDecimal USAGE_WARNING_THRESHOLD = BigDecimal.valueOf(80);
    private static final BigDecimal USAGE_BLOCK_THRESHOLD = BigDecimal.valueOf(100);

    private final BudgetRepository budgetRepository;

    @Transactional(readOnly = true)
    public Page<BudgetResponseDTO> listBudgets(Pageable pageable) {
        Long tenantId = TenantContext.getRequiredTenantId();
        return budgetRepository.findAllByTenant_Id(tenantId, pageable).map(this::toResponse);
    }

    @Transactional
    public void validateBudgetBeforeFinalApproval(Expense expense) {
        Long tenantId = TenantContext.getRequiredTenantId();
        if (!tenantId.equals(expense.getTenant().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant mismatch for budget validation");
        }
        LocalDate expenseDate = expense.getExpenseDate();

        Budget budget = budgetRepository.findActiveBudgetForDate(tenantId, expenseDate)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "No active budget found for the expense period"
            ));

        BigDecimal allocatedAmount = nonNullMoney(budget.getAllocatedAmount());
        if (allocatedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Allocated budget amount must be greater than zero");
        }

        BigDecimal projectedSpent = nonNullMoney(budget.getSpentAmount()).add(nonNullMoney(expense.getAmount()));
        BigDecimal usagePercent = projectedSpent
            .multiply(BigDecimal.valueOf(100))
            .divide(allocatedAmount, 2, RoundingMode.HALF_UP);

        if (usagePercent.compareTo(USAGE_WARNING_THRESHOLD) >= 0) {
            log.warn(
                "Budget usage warning: tenantId={}, budgetId={}, usagePercent={}, projectedSpent={}, allocated={}",
                tenantId,
                budget.getId(),
                usagePercent,
                projectedSpent,
                allocatedAmount
            );
        }

        if (projectedSpent.compareTo(allocatedAmount) > 0 || usagePercent.compareTo(USAGE_BLOCK_THRESHOLD) >= 0) {
            throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Budget exceeded: approval blocked for this expense"
            );
        }
    }

    private BigDecimal nonNullMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BudgetResponseDTO toResponse(Budget budget) {
        return BudgetResponseDTO.builder()
            .id(budget.getId())
            .tenantId(budget.getTenant().getId())
            .name(budget.getName())
            .periodStart(budget.getPeriodStart())
            .periodEnd(budget.getPeriodEnd())
            .allocatedAmount(budget.getAllocatedAmount())
            .spentAmount(budget.getSpentAmount())
            .currencyCode(budget.getCurrencyCode())
            .status(budget.getStatus())
            .createdAt(budget.getCreatedAt())
            .updatedAt(budget.getUpdatedAt())
            .build();
    }
}
