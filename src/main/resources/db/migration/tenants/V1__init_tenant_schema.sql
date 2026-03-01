-- V1__init_tenant_schema.sql
-- Sub-schema that is dynamically replicated for each RESTAURANT

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE zone (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE restaurant_table (
    id UUID PRIMARY KEY,
    zone_id UUID NOT NULL REFERENCES zone(id) ON DELETE CASCADE,
    table_number VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE category (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE product (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    price NUMERIC(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE restaurant_order (
    id UUID PRIMARY KEY,
    table_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(255),
    subtotal NUMERIC(12,2) NOT NULL,
    tax NUMERIC(12,2) NOT NULL,
    total NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE restaurant_order_item (
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

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES restaurant_order(id) ON DELETE CASCADE,
    amount NUMERIC(10,2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    reference_notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
