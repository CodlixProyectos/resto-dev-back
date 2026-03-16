-- V1_8__create_pensioner_payments_table.sql
CREATE TABLE admin.pensioner_payments (
    id UUID PRIMARY KEY,
    pensioner_id UUID NOT NULL REFERENCES admin.pensioners(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'EFECTIVO',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_pensioner_payments_pensioner ON admin.pensioner_payments(pensioner_id);
CREATE INDEX idx_pensioner_payments_date ON admin.pensioner_payments(date);
