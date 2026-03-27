-- Add sunat API credentials (Client ID and Client Secret) to organizations table

ALTER TABLE organizations
ADD COLUMN sunat_client_id VARCHAR(100),
ADD COLUMN sunat_client_secret VARCHAR(255);
