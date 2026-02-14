CREATE TABLE tenants (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    status VARCHAR(30) NOT NULL,
    currency_code CHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tenants_code UNIQUE (code)
);

CREATE INDEX idx_tenants_status ON tenants (status);
