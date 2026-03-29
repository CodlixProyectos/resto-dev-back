CREATE TABLE pensioners (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    dni VARCHAR(20),
    email VARCHAR(255),
    phone_number VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE pensioner_consumptions (
    id UUID PRIMARY KEY,
    pensioner_id UUID NOT NULL REFERENCES pensioners(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(255),
    total_amount DECIMAL(10, 2),
    is_extra BOOLEAN NOT NULL DEFAULT false,
    items_snapshot TEXT,
    notes TEXT,
    payment_type VARCHAR(50),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pensioner_payments (
    id UUID PRIMARY KEY,
    pensioner_id UUID NOT NULL REFERENCES pensioners(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
