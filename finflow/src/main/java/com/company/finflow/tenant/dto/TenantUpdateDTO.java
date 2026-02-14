package com.company.finflow.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TenantUpdateDTO {

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    @Size(max = 30)
    private String status;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currencyCode;
}
