package com.company.finflow.expense.dto;

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
public class ExpenseCreateDTO {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 4000)
    private String description;

    @NotBlank
    @Size(max = 80)
    private String category;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currencyCode;

    @NotNull
    private LocalDate expenseDate;

    @Size(max = 1024)
    private String receiptUrl;
}
