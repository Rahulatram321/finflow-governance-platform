package com.company.finflow.audit.repository;

import com.company.finflow.audit.domain.AuditLog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByTenant_Id(Long tenantId);

    Page<AuditLog> findAllByTenant_Id(Long tenantId, Pageable pageable);

    Optional<AuditLog> findByIdAndTenant_Id(Long id, Long tenantId);
}
