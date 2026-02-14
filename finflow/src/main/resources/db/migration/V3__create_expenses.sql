CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    submitted_by_user_id BIGINT NOT NULL,
    approved_by_user_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(80) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    currency_code CHAR(3) NOT NULL,
    expense_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    receipt_url VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_expenses_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_expenses_submitted_by_user FOREIGN KEY (submitted_by_user_id) REFERENCES users (id),
    CONSTRAINT fk_expenses_approved_by_user FOREIGN KEY (approved_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_expenses_amount_non_negative CHECK (amount >= 0)
);

CREATE INDEX idx_expenses_tenant_id ON expenses (tenant_id);
CREATE INDEX idx_expenses_status ON expenses (status);
CREATE INDEX idx_expenses_expense_date ON expenses (expense_date);
CREATE INDEX idx_expenses_submitted_by_user_id ON expenses (submitted_by_user_id);
