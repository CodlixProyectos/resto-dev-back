-- V5__increase_schema_name_length.sql
ALTER TABLE admin.organizations ALTER COLUMN schema_name TYPE VARCHAR(63);
