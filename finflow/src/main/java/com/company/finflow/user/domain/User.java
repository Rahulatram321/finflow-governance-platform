package com.company.finflow.user.domain;

import com.company.finflow.audit.domain.AuditLog;
import com.company.finflow.common.persistence.BaseTenantEntity;
import com.company.finflow.expense.domain.Expense;
import com.company.finflow.workflow.domain.WorkflowStep;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
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
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_users_tenant_email", columnNames = {"tenant_id", "email"})
    }
)
public class User extends BaseTenantEntity {

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 40)
    private String role;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @JsonIgnore
    @OneToMany(mappedBy = "submittedBy", fetch = FetchType.LAZY)
    private List<Expense> submittedExpenses = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "approvedBy", fetch = FetchType.LAZY)
    private List<Expense> approvedExpenses = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY)
    private List<WorkflowStep> assignedWorkflowSteps = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs = new ArrayList<>();
}
