package com.company.finflow.user.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponseDTO {
    Long id;
    Long tenantId;
    String email;
    String firstName;
    String lastName;
    String role;
    String status;
    Instant lastLoginAt;
    Instant createdAt;
    Instant updatedAt;
}
