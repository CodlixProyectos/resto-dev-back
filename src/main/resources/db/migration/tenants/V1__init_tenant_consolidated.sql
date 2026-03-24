-- V1__init_tenant_consolidated.sql
-- Consolidated Schema for Tenant/Restaurant modules (Menu, Tables, Sales, Pensioners, Inventory)
-- This schema is dynamically replicated for each organization/restaurant.

-- Using gen_random_uuid() which is native in PostgreSQL 13+
-- No need for uuid-ossp extension in each tenant schema

-- 1. Layout: Zones and Tables
CREATE TABLE IF NOT EXISTS zone (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    entrance_pos_x DOUBLE PRECISION,
    entrance_pos_y DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS restaurant_table (
    id UUID PRIMARY KEY,
    zone_id UUID NOT NULL REFERENCES zone(id) ON DELETE CASCADE,
    table_number VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    pos_x DOUBLE PRECISION,
    pos_y DOUBLE PRECISION,
    width DOUBLE PRECISION,
    height DOUBLE PRECISION,
    rotation INTEGER,
    shape VARCHAR(20)
);

-- 2. Menu: Categories and Products (Front-facing menu)
CREATE TABLE IF NOT EXISTS category (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    price NUMERIC(10,2) NOT NULL,
    image_url TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- 3. Sales: Orders and Items
CREATE TABLE IF NOT EXISTS restaurant_order (
    id UUID PRIMARY KEY,
    table_id UUID REFERENCES restaurant_table(id) ON DELETE SET NULL,
    table_number VARCHAR(50), -- Denormalized for history
    waiter_id UUID, -- References global admin.users.id
    status VARCHAR(30) NOT NULL,
    order_type VARCHAR(30) NOT NULL DEFAULT 'DINE_IN',
    customer_name VARCHAR(150),
    notes VARCHAR(255),
    subtotal NUMERIC(12,2) NOT NULL,
    tax NUMERIC(12,2) NOT NULL,
    total NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Indexes for performance (Order history and KDS)
CREATE INDEX IF NOT EXISTS idx_restaurant_order_waiter_created ON restaurant_order (waiter_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_restaurant_order_status_created ON restaurant_order (status, created_at ASC);

CREATE TABLE IF NOT EXISTS restaurant_order_item (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES restaurant_order(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    quantity INT NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL,
    notes VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- 4. Finance: Payments and Cash Shifts
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES restaurant_order(id) ON DELETE CASCADE,
    amount NUMERIC(10,2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    reference_notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    evidence_url TEXT
);

CREATE TABLE IF NOT EXISTS cash_shifts (
    id UUID PRIMARY KEY,
    opened_by UUID NOT NULL, -- References global admin.users.id
    opened_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP,
    starting_cash NUMERIC(10, 2) NOT NULL,
    expected_cash NUMERIC(10, 2),
    actual_cash NUMERIC(10, 2),
    difference NUMERIC(10, 2),
    status VARCHAR(20) NOT NULL
);

-- 5. Pensioners System
CREATE TABLE IF NOT EXISTS pensioners (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    dni VARCHAR(20),
    email VARCHAR(255),
    phone_number VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pensioners_active ON pensioners(active);

CREATE TABLE IF NOT EXISTS pensioner_consumptions (
    id UUID PRIMARY KEY,
    pensioner_id UUID NOT NULL REFERENCES pensioners(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(500),
    total_amount DECIMAL(10,2) DEFAULT 0.00,
    is_extra BOOLEAN NOT NULL DEFAULT FALSE,
    payment_type VARCHAR(50) DEFAULT 'EFECTIVO',
    items_snapshot TEXT,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pensioner_consumptions_pensioner ON pensioner_consumptions(pensioner_id);
CREATE INDEX IF NOT EXISTS idx_pensioner_consumptions_date ON pensioner_consumptions(date);

CREATE TABLE IF NOT EXISTS pensioner_payments (
    id UUID PRIMARY KEY,
    pensioner_id UUID NOT NULL REFERENCES pensioners(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'EFECTIVO',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pensioner_payments_pensioner ON pensioner_payments(pensioner_id);
CREATE INDEX IF NOT EXISTS idx_pensioner_payments_date ON pensioner_payments(date);

-- 6. Inventory Module
CREATE TABLE IF NOT EXISTS inventory_suppliers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(150),
    phone VARCHAR(50),
    email VARCHAR(255),
    address TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    color VARCHAR(50) DEFAULT '#10B981', -- Emerald 500 by default
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_items (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    category_id UUID REFERENCES inventory_categories(id) ON DELETE SET NULL,
    unit VARCHAR(20) NOT NULL, -- kg, lt, un, etc.
    current_stock NUMERIC(12,3) DEFAULT 0.000,
    min_stock NUMERIC(12,3) DEFAULT 0.000,
    max_stock NUMERIC(12,3) DEFAULT 0.000,
    cost_price NUMERIC(12,2) DEFAULT 0.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_inventory_items_category_id ON inventory_items(category_id);

CREATE TABLE IF NOT EXISTS inventory_movements (
    id UUID PRIMARY KEY,
    item_id UUID NOT NULL REFERENCES inventory_items(id) ON DELETE CASCADE,
    supplier_id UUID REFERENCES inventory_suppliers(id) ON DELETE SET NULL,
    type VARCHAR(30) NOT NULL, -- COMPRA, CONSUMO, AJUSTE, MERMA
    quantity NUMERIC(12,3) NOT NULL,
    unit_price NUMERIC(12,2), -- Historical cost at the time of movement
    date TIMESTAMP NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_inventory_movements_item ON inventory_movements(item_id, date DESC);
