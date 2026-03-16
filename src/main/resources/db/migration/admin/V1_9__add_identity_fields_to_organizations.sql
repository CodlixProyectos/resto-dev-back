-- V1_9__add_identity_fields_to_organizations.sql
-- Add contact and fiscal identity fields to the organizations table

ALTER TABLE admin.organizations
ADD COLUMN legal_name VARCHAR(255),
ADD COLUMN business_id VARCHAR(50),
ADD COLUMN email VARCHAR(255),
ADD COLUMN phone VARCHAR(50),
ADD COLUMN address TEXT,
ADD COLUMN logo_url TEXT;

-- Comments for documentation
COMMENT ON COLUMN admin.organizations.legal_name IS 'Legal name of the entity for tax purposes';
COMMENT ON COLUMN admin.organizations.business_id IS 'Tax Identification Number (RUC, NIT, etc.)';
COMMENT ON COLUMN admin.organizations.logo_url IS 'URL or path to the organization logo image';
