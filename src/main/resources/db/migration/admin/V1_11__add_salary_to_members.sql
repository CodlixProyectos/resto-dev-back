-- V1_11__add_salary_and_status_to_members.sql
-- Add salary and status fields to organization members
ALTER TABLE admin.organization_members
ADD COLUMN salary NUMERIC(10, 2),
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

COMMENT ON COLUMN admin.organization_members.salary IS 'Monthly salary for the organization member';
COMMENT ON COLUMN admin.organization_members.status IS 'Current status of the member (ACTIVE, ON_LEAVE, INACTIVE)';
