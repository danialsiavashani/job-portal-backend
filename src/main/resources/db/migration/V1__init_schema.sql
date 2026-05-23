-- =============================================================================
-- V1: Clean bootstrap schema
-- Replaces non-bootstrapable patch migrations V1–V5.
-- Ground truth: docs/schema-dump-local.sql (captured 2026-05-22)
--
-- Intentional differences from the live DB:
--   (1) VARCHAR used for all enum columns instead of MySQL ENUM.
--       Reason: matches JPA @Enumerated(EnumType.STRING); avoids ALTER COLUMN
--       on every new enum value; passes ddl-auto=validate cleanly.
--   (2) Three UNIQUE constraints added that were missing from the live DB:
--         uk_users_username   on users.username
--         uk_users_email      on users.email
--         uk_roles_role_name  on roles.role_name
--   (3) degree_fields.name has exactly ONE unique constraint (uk_degree_field_name).
--       The live DB has a duplicate Hibernate-generated key; it is omitted here.
--   (4) job_applications.document_public_id and document_url are intentionally
--       absent. These columns exist in the live DB but are not mapped by the
--       JobApplication entity (cloud upload was removed). They will be dropped
--       from the live DB when the Option B DB reset is performed.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. roles
--    No upstream dependencies.
-- -----------------------------------------------------------------------------
CREATE TABLE roles (
    role_id   BIGINT      NOT NULL AUTO_INCREMENT,
    role_name VARCHAR(20) DEFAULT NULL,
    PRIMARY KEY (role_id),
    CONSTRAINT uk_roles_role_name UNIQUE (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 2. users
--    Depends on: roles
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    user_id                 BIGINT       NOT NULL AUTO_INCREMENT,
    username                VARCHAR(100) NOT NULL,
    email                   VARCHAR(50)  NOT NULL,
    password_hash           VARCHAR(120) DEFAULT NULL,
    role_id                 BIGINT       NOT NULL,
    account_non_locked      BIT(1)       NOT NULL,
    account_non_expired     BIT(1)       NOT NULL,
    credentials_non_expired BIT(1)       NOT NULL,
    enabled                 BIT(1)       NOT NULL,
    credentials_expiry_date DATE         DEFAULT NULL,
    account_expiry_date     DATE         DEFAULT NULL,
    two_factor_secret       VARCHAR(64)  DEFAULT NULL,
    two_factor_enabled      BIT(1)       NOT NULL,
    sign_up_method          VARCHAR(20)  DEFAULT NULL,
    created_date            DATETIME(6)  DEFAULT NULL,
    updated_date            DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email    UNIQUE (email),
    CONSTRAINT fk_users_role     FOREIGN KEY (role_id) REFERENCES roles (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 3. degree_fields
--    No upstream dependencies.
-- -----------------------------------------------------------------------------
CREATE TABLE degree_fields (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    name   VARCHAR(120) NOT NULL,
    active BIT(1)       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_degree_field_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 4. companies
--    Depends on: users
-- -----------------------------------------------------------------------------
CREATE TABLE companies (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(150) NOT NULL,
    owner_id       BIGINT       NOT NULL,
    logo_public_id VARCHAR(255) DEFAULT NULL,
    logo_url       VARCHAR(500) DEFAULT NULL,
    enabled        BIT(1)       NOT NULL,
    created_at     DATETIME(6)  DEFAULT NULL,
    updated_at     DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_companies_owner_id UNIQUE (owner_id),
    CONSTRAINT fk_companies_owner    FOREIGN KEY (owner_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 5. company_applications
--    Depends on: users
--    document_public_id / document_url are kept: they ARE mapped in the
--    CompanyApplication entity (verification doc for the company sign-up flow).
-- -----------------------------------------------------------------------------
CREATE TABLE company_applications (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    user_id            BIGINT       NOT NULL,
    company_name       VARCHAR(150) NOT NULL,
    document_public_id VARCHAR(255) DEFAULT NULL,
    document_url       VARCHAR(500) DEFAULT NULL,
    status             VARCHAR(30)  NOT NULL,
    created_at         DATETIME(6)  NOT NULL,
    updated_at         DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_company_applications_user_id UNIQUE (user_id),
    CONSTRAINT fk_company_applications_user    FOREIGN KEY (user_id) REFERENCES users (user_id),
    INDEX idx_status_created_at (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 6. candidate_profiles
--    Depends on: users, degree_fields
--    Shared PK: user_id is both PK and FK to users.user_id (@MapsId).
-- -----------------------------------------------------------------------------
CREATE TABLE candidate_profiles (
    user_id          BIGINT       NOT NULL,
    education_level  VARCHAR(30)  DEFAULT NULL,
    location         VARCHAR(120) DEFAULT NULL,
    phone            VARCHAR(30)  DEFAULT NULL,
    resume_public_id VARCHAR(255) DEFAULT NULL,
    resume_url       VARCHAR(500) DEFAULT NULL,
    years_experience DECIMAL(4,1) DEFAULT NULL,
    degree_field_id  BIGINT       DEFAULT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_candidate_profiles_user         FOREIGN KEY (user_id)        REFERENCES users (user_id),
    CONSTRAINT fk_candidate_profiles_degree_field FOREIGN KEY (degree_field_id) REFERENCES degree_fields (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 7. candidate_certificates
--    Depends on: candidate_profiles
-- -----------------------------------------------------------------------------
CREATE TABLE candidate_certificates (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    profile_user_id BIGINT       NOT NULL,
    name            VARCHAR(255) NOT NULL,
    issuer          VARCHAR(255) DEFAULT NULL,
    issue_date      DATE         DEFAULT NULL,
    expiry_date     DATE         DEFAULT NULL,
    credential_url  VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_candidate_certificates_profile
        FOREIGN KEY (profile_user_id) REFERENCES candidate_profiles (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 8. candidate_experiences
--    Depends on: candidate_profiles
-- -----------------------------------------------------------------------------
CREATE TABLE candidate_experiences (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    profile_user_id BIGINT       NOT NULL,
    type            VARCHAR(30)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    company_name    VARCHAR(255) DEFAULT NULL,
    start_date      DATE         DEFAULT NULL,
    end_date        DATE         DEFAULT NULL,
    description     TEXT         DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_candidate_experiences_profile
        FOREIGN KEY (profile_user_id) REFERENCES candidate_profiles (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 9. jobs
--    Depends on: companies
-- -----------------------------------------------------------------------------
CREATE TABLE jobs (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    company_id           BIGINT        NOT NULL,
    title                VARCHAR(255)  NOT NULL,
    description          TEXT          NOT NULL,
    tagline              VARCHAR(255)  DEFAULT NULL,
    employment_type      VARCHAR(30)   NOT NULL,
    level                VARCHAR(255)  DEFAULT NULL,
    location             VARCHAR(255)  DEFAULT NULL,
    number_of_applicants INT           NOT NULL,
    pay_min              DECIMAL(12,2) DEFAULT NULL,
    pay_max              DECIMAL(12,2) DEFAULT NULL,
    pay_period           VARCHAR(10)   DEFAULT NULL,
    pay_type             VARCHAR(20)   DEFAULT NULL,
    status               VARCHAR(20)   NOT NULL,
    created_at           DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_jobs_company FOREIGN KEY (company_id) REFERENCES companies (id),
    INDEX idx_jobs_status_created_at         (status,     created_at),
    INDEX idx_jobs_company_created_at        (company_id, created_at),
    INDEX idx_jobs_company_status_created_at (company_id, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 10. job_benefits  (element collection — no PK row, no explicit index needed;
--     MySQL auto-creates an index on the FK column)
--     Depends on: jobs
-- -----------------------------------------------------------------------------
CREATE TABLE job_benefits (
    job_id  BIGINT       NOT NULL,
    benefit VARCHAR(255) DEFAULT NULL,
    CONSTRAINT fk_job_benefits_job
        FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 11. job_minimum_requirements  (element collection — same pattern as above)
--     Depends on: jobs
-- -----------------------------------------------------------------------------
CREATE TABLE job_minimum_requirements (
    job_id      BIGINT       NOT NULL,
    requirement VARCHAR(255) DEFAULT NULL,
    CONSTRAINT fk_job_minimum_requirements_job
        FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 12. job_degree_fields  (M:M join table)
--     Depends on: jobs, degree_fields
-- -----------------------------------------------------------------------------
CREATE TABLE job_degree_fields (
    job_id          BIGINT NOT NULL,
    degree_field_id BIGINT NOT NULL,
    PRIMARY KEY (job_id, degree_field_id),
    CONSTRAINT fk_job_degree_fields_job
        FOREIGN KEY (job_id)          REFERENCES jobs (id),
    CONSTRAINT fk_job_degree_fields_degree_field
        FOREIGN KEY (degree_field_id) REFERENCES degree_fields (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 13. job_applications
--     Depends on: users, jobs, companies
--     OMITS document_public_id and document_url (see file header, point 4).
-- -----------------------------------------------------------------------------
CREATE TABLE job_applications (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    job_id     BIGINT      NOT NULL,
    company_id BIGINT      NOT NULL,
    status     VARCHAR(20) NOT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_job_application_job_user UNIQUE (job_id, user_id),
    CONSTRAINT fk_job_applications_user    FOREIGN KEY (user_id)    REFERENCES users (user_id),
    CONSTRAINT fk_job_applications_job     FOREIGN KEY (job_id)     REFERENCES jobs (id),
    CONSTRAINT fk_job_applications_company FOREIGN KEY (company_id) REFERENCES companies (id),
    INDEX idx_job_app_company_created_at        (company_id, created_at),
    INDEX idx_job_app_company_status_created_at (company_id, status, created_at),
    INDEX idx_job_app_user_created_at           (user_id,    created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 14. job_application_status_audits
--     Depends on: job_applications
--     from_status / to_status use VARCHAR (not ENUM): matches entity @Column(length=50)
--     and is consistent with the live DB which already uses VARCHAR here.
-- -----------------------------------------------------------------------------
CREATE TABLE job_application_status_audits (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    application_id BIGINT        NOT NULL,
    company_id     BIGINT        NOT NULL,
    actor_user_id  BIGINT        NOT NULL,
    from_status    VARCHAR(50)   NOT NULL,
    to_status      VARCHAR(50)   NOT NULL,
    note           VARCHAR(1000) DEFAULT NULL,
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_audit_application
        FOREIGN KEY (application_id) REFERENCES job_applications (id) ON DELETE CASCADE,
    INDEX idx_audit_application_created_at (application_id, created_at),
    INDEX idx_audit_company_created_at     (company_id,     created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 15. saved_jobs
--     Depends on: users, jobs
-- -----------------------------------------------------------------------------
CREATE TABLE saved_jobs (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    job_id     BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_saved_job_user_job UNIQUE (user_id, job_id),
    CONSTRAINT fk_saved_jobs_user   FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_saved_jobs_job    FOREIGN KEY (job_id)  REFERENCES jobs (id),
    INDEX idx_saved_jobs_user_created_at (user_id, created_at),
    INDEX idx_saved_jobs_job             (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -----------------------------------------------------------------------------
-- 16. password_reset_tokens
--     Depends on: users
-- -----------------------------------------------------------------------------
CREATE TABLE password_reset_tokens (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    token_hash VARCHAR(64)  NOT NULL,
    user_id    BIGINT       NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    used_at    TIMESTAMP(6) DEFAULT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_prt_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_prt_user       FOREIGN KEY (user_id) REFERENCES users (user_id),
    INDEX idx_prt_user_id    (user_id),
    INDEX idx_prt_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
