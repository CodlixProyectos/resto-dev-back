-- V1__init_admin_consolidated.sql
-- Consolidated Schema for Admin/Global modules (Users, Organizations, Auth)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Users table (Global identity)
CREATE TABLE IF NOT EXISTS admin.users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    dni VARCHAR(20),
    phone_number VARCHAR(20),
    avatar_url VARCHAR(500),
    super_admin BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sound_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    dark_mode_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Optimization for PIN/DNI login
CREATE INDEX IF NOT EXISTS idx_users_dni ON admin.users(dni);

-- 2. Organizations / Tenants
CREATE TABLE IF NOT EXISTS admin.organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(60) UNIQUE NOT NULL,
    schema_name VARCHAR(30) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'restaurant',
    owner_id UUID NOT NULL,
    legal_name VARCHAR(255),
    business_id VARCHAR(50),
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    logo_url TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- 3. Roles and Permissions
CREATE TABLE IF NOT EXISTS admin.roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin.permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(80) UNIQUE NOT NULL,
    description VARCHAR(255),
    module VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin.role_permissions (
    role_id UUID NOT NULL REFERENCES admin.roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES admin.permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 4. Subscription Plans
CREATE TABLE IF NOT EXISTS admin.subscription_plans (
    id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    max_tables INT NOT NULL,
    max_users INT NOT NULL,
    price_monthly NUMERIC(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin.organization_subscriptions (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES admin.organizations(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES admin.subscription_plans(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
    start_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- 5. Membership (Users in Organizations)
CREATE TABLE IF NOT EXISTS admin.organization_members (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES admin.organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES admin.users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES admin.roles(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    salary NUMERIC(10, 2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    pin VARCHAR(255),
    joined_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_member_org_user_role UNIQUE (organization_id, user_id, role_id)
);

-- Indexes for Admin Auth
CREATE INDEX IF NOT EXISTS idx_org_members_user_pin ON admin.organization_members(user_id, pin) WHERE active = true;
CREATE INDEX IF NOT EXISTS idx_org_members_org_pin ON admin.organization_members(organization_id, pin) WHERE active = true;

COMMENT ON COLUMN admin.organizations.legal_name IS 'Legal name of the entity for tax purposes';
COMMENT ON COLUMN admin.organizations.business_id IS 'Tax Identification Number (RUC, NIT, etc.)';
COMMENT ON COLUMN admin.organizations.logo_url IS 'URL or path to the organization logo image';
COMMENT ON COLUMN admin.organization_members.salary IS 'Monthly salary for the organization member';
COMMENT ON COLUMN admin.organization_members.status IS 'Current status of the member (ACTIVE, ON_LEAVE, INACTIVE)';
