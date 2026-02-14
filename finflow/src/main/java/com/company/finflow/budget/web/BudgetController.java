package com.company.finflow.budget.web;

import com.company.finflow.budget.domain.Budget;
import com.company.finflow.budget.dto.BudgetCreateDTO;
import com.company.finflow.budget.dto.BudgetResponseDTO;
import com.company.finflow.budget.dto.BudgetUpdateDTO;
import com.company.finflow.budget.repository.BudgetRepository;
import com.company.finflow.budget.service.BudgetService;
import com.company.finflow.common.web.ApiResponse;
import com.company.finflow.common.web.PaginatedResponse;
import com.company.finflow.tenant.domain.Tenant;
import com.company.finflow.tenant.context.TenantContext;
import com.company.finflow.tenant.repository.TenantRepository;
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

@Tag(name = "Budget")
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetRepository budgetRepository;
    private final TenantRepository tenantRepository;
    private final BudgetService budgetService;

    @Operation(
        summary = "List budgets for current tenant",
        description = "Supports pagination and sorting via query params: page, size, sort (e.g. sort=createdAt,desc)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<BudgetResponseDTO>>> list(
        @Parameter(hidden = true)
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.from(budgetService.listBudgets(pageable))));
    }

    @Operation(summary = "Get budget by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponseDTO>> get(@PathVariable Long id) {
        Long tenantId = TenantContext.getRequiredTenantId();
        return ResponseEntity.ok(ApiResponse.success(toResponse(findBudget(id, tenantId))));
    }

    @Operation(summary = "Create budget")
    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponseDTO>> create(@Valid @RequestBody BudgetCreateDTO request) {
        Long tenantId = TenantContext.getRequiredTenantId();
        Budget budget = new Budget();
        map(budget, request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toResponse(budgetRepository.save(budget))));
    }

    @Operation(summary = "Update budget")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponseDTO>> update(@PathVariable Long id, @Valid @RequestBody BudgetUpdateDTO request) {
        Long tenantId = TenantContext.getRequiredTenantId();
        Budget budget = findBudget(id, tenantId);
        map(budget, request, tenantId);
        return ResponseEntity.ok(ApiResponse.success(toResponse(budgetRepository.save(budget))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Long tenantId = TenantContext.getRequiredTenantId();
        Budget budget = findBudget(id, tenantId);
        budgetRepository.delete(budget);
        return ResponseEntity.ok(ApiResponse.success(null, "Budget deleted"));
    }

    private Budget findBudget(Long id, Long tenantId) {
        return budgetRepository.findByIdAndTenant_Id(id, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
    }

    private Tenant findTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
    }

    private void map(Budget budget, BudgetCreateDTO request, Long tenantId) {
        budget.setTenant(findTenant(tenantId));
        budget.setName(request.getName().trim());
        budget.setPeriodStart(request.getPeriodStart());
        budget.setPeriodEnd(request.getPeriodEnd());
        budget.setAllocatedAmount(request.getAllocatedAmount());
        budget.setSpentAmount(request.getSpentAmount());
        budget.setCurrencyCode(request.getCurrencyCode().trim().toUpperCase());
        budget.setStatus(request.getStatus().trim().toUpperCase());
    }

    private void map(Budget budget, BudgetUpdateDTO request, Long tenantId) {
        budget.setTenant(findTenant(tenantId));
        budget.setName(request.getName().trim());
        budget.setPeriodStart(request.getPeriodStart());
        budget.setPeriodEnd(request.getPeriodEnd());
        budget.setAllocatedAmount(request.getAllocatedAmount());
        budget.setSpentAmount(request.getSpentAmount());
        budget.setCurrencyCode(request.getCurrencyCode().trim().toUpperCase());
        budget.setStatus(request.getStatus().trim().toUpperCase());
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
