package com.company.finflow.user.service;

import com.company.finflow.tenant.context.TenantContext;
import com.company.finflow.user.domain.User;
import com.company.finflow.user.dto.UserResponseDTO;
import com.company.finflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> listUsers(Pageable pageable) {
        Long tenantId = TenantContext.getRequiredTenantId();
        return userRepository.findAllByTenant_Id(tenantId, pageable).map(this::toResponse);
    }

    private UserResponseDTO toResponse(User user) {
        return UserResponseDTO.builder()
            .id(user.getId())
            .tenantId(user.getTenant().getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole())
            .status(user.getStatus())
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
