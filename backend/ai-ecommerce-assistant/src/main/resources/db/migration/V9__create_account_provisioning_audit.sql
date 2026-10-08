CREATE TABLE account_provisioning_audit (
    id BIGSERIAL PRIMARY KEY,
    attempted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    outcome VARCHAR(10) NOT NULL,
    reason_code VARCHAR(50) NOT NULL,
    test_user_requested BOOLEAN NOT NULL,
    admin_user_id BIGINT,
    test_user_id BIGINT,

    CONSTRAINT chk_account_provisioning_audit_outcome
        CHECK (outcome IN ('SUCCESS', 'FAILURE'))
);
