-- V1_7__add_payment_type_to_pensioner_consumptions.sql
ALTER TABLE admin.pensioner_consumptions ADD COLUMN payment_type VARCHAR(50) DEFAULT 'EFECTIVO';
