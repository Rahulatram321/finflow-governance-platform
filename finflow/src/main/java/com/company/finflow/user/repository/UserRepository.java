package com.company.finflow.user.repository;

import com.company.finflow.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByTenant_Id(Long tenantId);

    Page<User> findAllByTenant_Id(Long tenantId, Pageable pageable);

    Optional<User> findByIdAndTenant_Id(Long id, Long tenantId);

    Optional<User> findFirstByTenant_IdAndRoleIgnoreCaseAndStatusIgnoreCase(Long tenantId, String role, String status);

    Optional<User> findFirstByTenant_IdAndRoleIgnoreCase(Long tenantId, String role);

    Optional<User> findByEmailIgnoreCaseAndTenant_Id(String email, Long tenantId);
}
