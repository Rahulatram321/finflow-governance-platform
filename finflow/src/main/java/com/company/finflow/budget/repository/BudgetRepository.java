package com.company.finflow.budget.repository;

import com.company.finflow.budget.domain.Budget;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findAllByTenant_Id(Long tenantId);

    Page<Budget> findAllByTenant_Id(Long tenantId, Pageable pageable);

    Optional<Budget> findByIdAndTenant_Id(Long id, Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select b
        from Budget b
        where b.tenant.id = :tenantId
          and upper(b.status) = 'ACTIVE'
          and :today between b.periodStart and b.periodEnd
        order by b.periodEnd asc
        """)
    Optional<Budget> findActiveBudgetForDate(@Param("tenantId") Long tenantId, @Param("today") LocalDate today);
}
