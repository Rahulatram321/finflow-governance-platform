package com.company.finflow.expense.repository;

import com.company.finflow.expense.domain.Expense;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByTenant_Id(Long tenantId);

    List<Expense> findAllByTenant_IdAndSubmittedBy_Id(Long tenantId, Long submittedById);

    Page<Expense> findAllByTenant_Id(Long tenantId, Pageable pageable);

    Page<Expense> findAllByTenant_IdAndSubmittedBy_Id(Long tenantId, Long submittedById, Pageable pageable);

    Optional<Expense> findByIdAndTenant_Id(Long id, Long tenantId);

    Optional<Expense> findByIdAndTenant_IdAndSubmittedBy_Id(Long id, Long tenantId, Long submittedById);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Expense e where e.id = :id and e.tenant.id = :tenantId")
    Optional<Expense> findByIdAndTenantIdForUpdate(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
