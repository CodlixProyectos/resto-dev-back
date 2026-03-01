-- V1_2__add_code_and_module_to_permissions.sql
-- Fix the permissions table to match the JPA Entity (PermissionEntity)

ALTER TABLE permissions RENAME COLUMN name TO code;
ALTER TABLE permissions ALTER COLUMN code TYPE VARCHAR(80);
ALTER TABLE permissions ADD COLUMN module VARCHAR(50) NOT NULL DEFAULT 'SYSTEM';
