-- V1 baseline: bring existing schema up to V1 rules (safe to run on non-empty DB)
-- Add unique constraint / unique index on degree_fields.name if it doesn't already exist.

SET @schema_name := DATABASE();

SET @has_unique := (
    SELECT COUNT(*)
    FROM information_schema.statistics s
    WHERE s.table_schema = @schema_name
      AND s.table_name = 'degree_fields'
      AND s.column_name = 'name'
      AND s.non_unique = 0
);

SET @sql := IF(
    @has_unique = 0,
    'ALTER TABLE degree_fields ADD CONSTRAINT uk_degree_fields_name UNIQUE (name)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
