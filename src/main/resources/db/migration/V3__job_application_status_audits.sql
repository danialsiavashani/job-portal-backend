CREATE TABLE IF NOT EXISTS job_application_status_audits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    actor_user_id BIGINT NOT NULL,
    from_status VARCHAR(50) NOT NULL,
    to_status VARCHAR(50) NOT NULL,
    note VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_application
        FOREIGN KEY (application_id) REFERENCES job_applications(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_audit_application_created_at
    ON job_application_status_audits (application_id, created_at);

CREATE INDEX idx_audit_company_created_at
    ON job_application_status_audits (company_id, created_at);
