-- V1_5__create_pensioners_table.sql
CREATE TABLE admin.pensioners (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES admin.organizations(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    dni VARCHAR(20),
    email VARCHAR(255),
    phone_number VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_pensioners_org ON admin.pensioners(organization_id);
