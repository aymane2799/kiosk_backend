CREATE TABLE plans (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tier VARCHAR(20),
    price INTEGER NOT NULL,
    currency VARCHAR(10) NOT NULL,
    billing_period INTEGER NOT NULL,

    tenant_id VARCHAR(36) NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_plans_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE INDEX idx_plans_tenant_id ON plans (tenant_id);