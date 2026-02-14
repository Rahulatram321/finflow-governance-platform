package com.company.finflow.workflow.domain;

import com.company.finflow.common.persistence.BaseTenantEntity;
import com.company.finflow.expense.domain.Expense;
import com.company.finflow.user.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "workflow_steps")
public class WorkflowStep extends BaseTenantEntity {

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_type", nullable = false, length = 60)
    private String stepType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkflowStepStatus status;

    @Column(name = "actioned_at")
    private Instant actionedAt;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_user_id")
    private User assignee;
}
