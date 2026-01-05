-- V2: enforce correctness rules for job_applications (safe to run on existing DB)
-- Adds UNIQUE(job_id, user_id) + common query indexes if missing.

SET @schema_name := DATABASE();

-- 1) Unique constraint: (job_id, user_id)
SET @has_uk := (
    SELECT COUNT(*)
    FROM information_schema.statistics s
    WHERE s.table_schema = @schema_name
      AND s.table_name = 'job_applications'
      AND s.index_name = 'uk_job_application_job_user'
      AND s.non_unique = 0
);

SET @sql := IF(
    @has_uk = 0,
    'ALTER TABLE job_applications ADD CONSTRAINT uk_job_application_job_user UNIQUE (job_id, user_id)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- 2) Index: (user_id, created_at)
SET @has_idx1 := (
    SELECT COUNT(*)
    FROM information_schema.statistics s
    WHERE s.table_schema = @schema_name
      AND s.table_name = 'job_applications'
      AND s.index_name = 'idx_job_app_user_created_at'
);

SET @sql := IF(
    @has_idx1 = 0,
    'CREATE INDEX idx_job_app_user_created_at ON job_applications (user_id, created_at)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- 3) Index: (company_id, created_at)
SET @has_idx2 := (
    SELECT COUNT(*)
    FROM information_schema.statistics s
    WHERE s.table_schema = @schema_name
      AND s.table_name = 'job_applications'
      AND s.index_name = 'idx_job_app_company_created_at'
);

SET @sql := IF(
    @has_idx2 = 0,
    'CREATE INDEX idx_job_app_company_created_at ON job_applications (company_id, created_at)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- 4) Index: (company_id, status, created_at)
SET @has_idx3 := (
    SELECT COUNT(*)
    FROM information_schema.statistics s
    WHERE s.table_schema = @schema_name
      AND s.table_name = 'job_applications'
      AND s.index_name = 'idx_job_app_company_status_created_at'
);

SET @sql := IF(
    @has_idx3 = 0,
    'CREATE INDEX idx_job_app_company_status_created_at ON job_applications (company_id, status, created_at)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
