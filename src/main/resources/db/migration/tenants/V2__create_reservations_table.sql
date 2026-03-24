-- V2__create_reservations_table.sql
-- Module: Reservations
-- Description: Table for managing restaurant table bookings.

CREATE TABLE IF NOT EXISTS reservation (
    id UUID PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50),
    customer_email VARCHAR(255),
    reservation_date DATE NOT NULL,
    reservation_time TIME NOT NULL,
    num_guests INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, CANCELLED, SEATED
    table_id UUID REFERENCES restaurant_table(id) ON DELETE SET NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reservation_date ON reservation(reservation_date);
CREATE INDEX IF NOT EXISTS idx_reservation_status ON reservation(status);
