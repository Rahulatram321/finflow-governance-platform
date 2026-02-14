package com.company.finflow.tenant.domain;

import com.company.finflow.audit.domain.AuditLog;
import com.company.finflow.budget.domain.Budget;
import com.company.finflow.common.persistence.BaseEntity;
import com.company.finflow.expense.domain.Expense;
import com.company.finflow.user.domain.User;
import com.company.finflow.workflow.domain.WorkflowStep;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "tenants",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_tenants_code", columnNames = "code")
    }
)
public class Tenant extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @JsonIgnore
    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY)
    private List<Expense> expenses = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY)
    private List<Budget> budgets = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY)
    private List<WorkflowStep> workflowSteps = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs = new ArrayList<>();
}
