-- Migration to add branding colors to organization
ALTER TABLE admin.organizations ADD COLUMN IF NOT EXISTS primary_color VARCHAR(20);
ALTER TABLE admin.organizations ADD COLUMN IF NOT EXISTS secondary_color VARCHAR(20);
