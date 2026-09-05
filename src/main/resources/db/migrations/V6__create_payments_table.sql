CREATE TABLE payments (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    amount INTEGER NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    external_ref VARCHAR(255) NOT NULL,
    processed_at TIMESTAMP,

    subscription_id VARCHAR(36) NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_payments_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions (id)
);

CREATE INDEX idx_payments_subscription_id ON payments (subscription_id);