package com.company.finflow.workflow.repository;

import com.company.finflow.workflow.domain.WorkflowStep;
import com.company.finflow.workflow.domain.WorkflowStepStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {

    List<WorkflowStep> findAllByTenant_Id(Long tenantId);

    Page<WorkflowStep> findAllByTenant_Id(Long tenantId, Pageable pageable);

    Optional<WorkflowStep> findByIdAndTenant_Id(Long id, Long tenantId);

    List<WorkflowStep> findAllByExpense_IdAndTenant_IdOrderByStepOrderAsc(Long expenseId, Long tenantId);

    List<WorkflowStep> findAllByExpense_IdAndTenant_IdAndStatusOrderByStepOrderAsc(
        Long expenseId,
        Long tenantId,
        WorkflowStepStatus status
    );

    boolean existsByExpense_IdAndTenant_Id(Long expenseId, Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ws from WorkflowStep ws where ws.id = :id and ws.tenant.id = :tenantId")
    Optional<WorkflowStep> findByIdAndTenantIdForUpdate(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
