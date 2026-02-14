package com.company.finflow.budget.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BudgetResponseDTO {
    Long id;
    Long tenantId;
    String name;
    LocalDate periodStart;
    LocalDate periodEnd;
    BigDecimal allocatedAmount;
    BigDecimal spentAmount;
    String currencyCode;
    String status;
    Instant createdAt;
    Instant updatedAt;
}
