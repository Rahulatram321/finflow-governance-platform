CREATE TABLE workflow_steps (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    expense_id BIGINT NOT NULL,
    step_order INTEGER NOT NULL,
    step_type VARCHAR(60) NOT NULL,
    assignee_user_id BIGINT,
    status VARCHAR(30) NOT NULL,
    actioned_at TIMESTAMPTZ,
    comments TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_workflow_steps_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_workflow_steps_expense FOREIGN KEY (expense_id) REFERENCES expenses (id) ON DELETE CASCADE,
    CONSTRAINT fk_workflow_steps_assignee_user FOREIGN KEY (assignee_user_id) REFERENCES users (id),
    CONSTRAINT chk_workflow_steps_step_order_positive CHECK (step_order > 0),
    CONSTRAINT uq_workflow_steps_expense_order UNIQUE (expense_id, step_order)
);

CREATE INDEX idx_workflow_steps_tenant_id ON workflow_steps (tenant_id);
CREATE INDEX idx_workflow_steps_expense_id ON workflow_steps (expense_id);
CREATE INDEX idx_workflow_steps_status ON workflow_steps (status);
CREATE INDEX idx_workflow_steps_assignee_user_id ON workflow_steps (assignee_user_id);
