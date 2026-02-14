CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    allocated_amount NUMERIC(19,4) NOT NULL,
    spent_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
    currency_code CHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_budgets_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT chk_budgets_period CHECK (period_end >= period_start),
    CONSTRAINT chk_budgets_allocated_amount_non_negative CHECK (allocated_amount >= 0),
    CONSTRAINT chk_budgets_spent_amount_non_negative CHECK (spent_amount >= 0),
    CONSTRAINT uq_budgets_tenant_name_period UNIQUE (tenant_id, name, period_start, period_end)
);

CREATE INDEX idx_budgets_tenant_id ON budgets (tenant_id);
CREATE INDEX idx_budgets_status ON budgets (status);
