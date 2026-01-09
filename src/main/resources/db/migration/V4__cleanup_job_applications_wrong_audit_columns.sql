SET @schema_name := DATABASE();

-- actor_user_id
SET @has := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema=@schema_name AND table_name='job_applications' AND column_name='actor_user_id'
);
SET @sql := IF(@has > 0, 'ALTER TABLE job_applications DROP COLUMN actor_user_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- application_id
SET @has := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema=@schema_name AND table_name='job_applications' AND column_name='application_id'
);
SET @sql := IF(@has > 0, 'ALTER TABLE job_applications DROP COLUMN application_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- from_status
SET @has := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema=@schema_name AND table_name='job_applications' AND column_name='from_status'
);
SET @sql := IF(@has > 0, 'ALTER TABLE job_applications DROP COLUMN from_status', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- to_status
SET @has := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema=@schema_name AND table_name='job_applications' AND column_name='to_status'
);
SET @sql := IF(@has > 0, 'ALTER TABLE job_applications DROP COLUMN to_status', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- note
SET @has := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema=@schema_name AND table_name='job_applications' AND column_name='note'
);
SET @sql := IF(@has > 0, 'ALTER TABLE job_applications DROP COLUMN note', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
