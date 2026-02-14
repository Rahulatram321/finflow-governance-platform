package com.company.finflow.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateDTO {

    @NotBlank
    @Email
    @Size(max = 254)
    private String email;

    @NotBlank
    @Size(max = 80)
    private String firstName;

    @NotBlank
    @Size(max = 80)
    private String lastName;

    @NotBlank
    @Size(max = 255)
    private String passwordHash;

    @NotBlank
    @Size(max = 40)
    @Pattern(
        regexp = "ADMIN|FINANCE_MANAGER|EMPLOYEE|AUDITOR",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "role must be one of ADMIN, FINANCE_MANAGER, EMPLOYEE, AUDITOR"
    )
    private String role;

    @NotBlank
    @Size(max = 30)
    @Pattern(
        regexp = "ACTIVE|INACTIVE",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "status must be ACTIVE or INACTIVE"
    )
    private String status;
}
