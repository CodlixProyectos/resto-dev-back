-- V1_6__create_pensioner_consumptions_table.sql
CREATE TABLE admin.pensioner_consumptions (
    id UUID PRIMARY KEY,
    pensioner_id UUID NOT NULL REFERENCES admin.pensioners(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(500),
    total_amount DECIMAL(10,2) DEFAULT 0.00,
    is_extra BOOLEAN NOT NULL DEFAULT FALSE,
    items_snapshot TEXT,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_pensioner_consumptions_pensioner ON admin.pensioner_consumptions(pensioner_id);
CREATE INDEX idx_pensioner_consumptions_date ON admin.pensioner_consumptions(date);
