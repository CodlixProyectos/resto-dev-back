-- V1_10__sync_organization_emails.sql
-- Pre-populate organization contact email with owner's registration email for existing records.

UPDATE admin.organizations o
SET email = (SELECT u.email FROM admin.users u WHERE u.id = o.owner_id)
WHERE o.email IS NULL;
