package com.company.finflow.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BudgetCreateDTO {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal allocatedAmount;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal spentAmount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currencyCode;

    @NotBlank
    @Size(max = 30)
    private String status;
}
