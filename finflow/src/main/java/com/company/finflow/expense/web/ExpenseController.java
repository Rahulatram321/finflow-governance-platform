package com.company.finflow.expense.web;

import com.company.finflow.common.web.ApiResponse;
import com.company.finflow.common.web.PaginatedResponse;
import com.company.finflow.expense.dto.ExpenseCreateDTO;
import com.company.finflow.expense.dto.ExpenseUpdateDTO;
import com.company.finflow.expense.dto.ExpenseWorkflowResponseDTO;
import com.company.finflow.expense.service.ExpenseService;
import com.company.finflow.workflow.dto.WorkflowStepRejectRequestDTO;
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

@Tag(name = "Expense Workflow")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(
        summary = "List expenses for current tenant",
        description = "Supports pagination and sorting via query params: page, size, sort (e.g. sort=createdAt,desc)"
    )
    @GetMapping("/expenses")
    public ResponseEntity<ApiResponse<PaginatedResponse<ExpenseWorkflowResponseDTO>>> list(
        @Parameter(hidden = true)
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(PaginatedResponse.from(expenseService.listExpenses(pageable)))
        );
    }

    @Operation(summary = "Get expense details with workflow")
    @GetMapping("/expenses/{id}")
    public ResponseEntity<ApiResponse<ExpenseWorkflowResponseDTO>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getExpense(id)));
    }

    @Operation(summary = "Create expense in DRAFT status")
    @PostMapping("/expenses")
    public ResponseEntity<ApiResponse<ExpenseWorkflowResponseDTO>> create(@Valid @RequestBody ExpenseCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(expenseService.createExpense(request)));
    }

    @Operation(summary = "Update draft/rejected expense")
    @PutMapping("/expenses/{id}")
    public ResponseEntity<ApiResponse<ExpenseWorkflowResponseDTO>> update(
        @PathVariable Long id,
        @Valid @RequestBody ExpenseUpdateDTO request
    ) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.updateExpense(id, request)));
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Expense deleted"));
    }

    @Operation(summary = "Submit expense for workflow approval")
    @PostMapping("/expenses/{id}/submit")
    public ResponseEntity<ApiResponse<ExpenseWorkflowResponseDTO>> submit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.submitExpense(id)));
    }

    @Operation(summary = "Approve workflow step")
    @PostMapping("/workflow-steps/{id}/approve")
    public ResponseEntity<ApiResponse<ExpenseWorkflowResponseDTO>> approveWorkflowStep(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.approveExpenseStep(id)));
    }

    @Operation(summary = "Reject workflow step")
    @PostMapping("/workflow-steps/{id}/reject")
    public ResponseEntity<ApiResponse<ExpenseWorkflowResponseDTO>> rejectWorkflowStep(
        @PathVariable Long id,
        @Valid @RequestBody WorkflowStepRejectRequestDTO request
    ) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.rejectExpenseStep(id, request.getReason())));
    }
}
