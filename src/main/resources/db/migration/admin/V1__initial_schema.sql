-- V1__initial_schema.sql
-- Consolidated Schema for Admin/Global modules (Users, Organizations, Auth, Subscriptions, Invitations)

-- 0. Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. Users table (Global identity)
CREATE TABLE IF NOT EXISTS admin.users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    dni VARCHAR(20),
    phone_number VARCHAR(20),
    avatar_url TEXT,
    super_admin BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sound_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    dark_mode_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_dni ON admin.users(dni);

-- 2. Organizations / Tenants
CREATE TABLE IF NOT EXISTS admin.organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(60) UNIQUE NOT NULL,
    schema_name VARCHAR(30) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'restaurant',
    owner_id UUID, -- Nullable to support invitation code registration
    legal_name VARCHAR(255),
    business_id VARCHAR(50),
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    logo_url TEXT,
    sunat_user VARCHAR(100),
    sunat_password VARCHAR(255),
    sunat_client_id VARCHAR(255),
    sunat_client_secret VARCHAR(255),
    primary_color VARCHAR(7) DEFAULT '#1e293b',
    secondary_color VARCHAR(7) DEFAULT '#64748b',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    invitation_code VARCHAR(20),
    has_inventory BOOLEAN NOT NULL DEFAULT FALSE,
    has_pensioners BOOLEAN NOT NULL DEFAULT FALSE,
    has_kds BOOLEAN NOT NULL DEFAULT FALSE,
    yape_qr_url TEXT,
    plin_qr_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_organizations_invitation_code ON admin.organizations(invitation_code);

-- 3. Roles and Permissions
CREATE TABLE IF NOT EXISTS admin.roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin.permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(80) UNIQUE NOT NULL,
    description VARCHAR(255),
    module VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin.organization_subscriptions (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES admin.organizations(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES admin.subscription_plans(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
    start_date DATE NOT NULL,
    end_date DATE,
    user_limit INT DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

COMMENT ON COLUMN admin.organization_subscriptions.user_limit IS 'Optional override for max users in this subscription. If null, plan default is used.';

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
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_member_org_user_role UNIQUE (organization_id, user_id, role_id)
);

CREATE INDEX IF NOT EXISTS idx_org_members_user_pin ON admin.organization_members(user_id, pin) WHERE active = true;
CREATE INDEX IF NOT EXISTS idx_org_members_org_pin ON admin.organization_members(organization_id, pin) WHERE active = true;

-- 6. Standalone Invitation Codes
CREATE TABLE IF NOT EXISTS admin.invitation_codes (
    id UUID PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    plan_name VARCHAR(50) NOT NULL,
    trial_days INT NOT NULL DEFAULT 30,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    used_by_organization_id UUID,
    used_by_organization_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Admin Notifications
CREATE TABLE IF NOT EXISTS admin.admin_notifications (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    action_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Organization Notifications
CREATE TABLE IF NOT EXISTS admin.notifications (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES admin.organizations(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    related_id VARCHAR(255),
    related_type VARCHAR(50),
    action_url TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_org_created ON admin.notifications(organization_id, created_at DESC);

-- SEEDS: Basic Security Data
INSERT INTO admin.permissions (id, code, description, module, created_at) VALUES 
('f1111111-1111-1111-1111-111111111111', 'MANAGE_MENU', 'Permite crear, editar y eliminar categorías y platos', 'MENU', NOW()),
('f2211111-1111-1111-1111-111111111111', 'VIEW_MENU', 'Permite visualizar la lista de platos y categorías', 'MENU', NOW()),
('f3311111-1111-1111-1111-111111111111', 'MANAGE_STAFF', 'Permite gestionar el personal y roles', 'ADMIN', NOW()),
('f4411111-1111-1111-1111-111111111111', 'VIEW_REPORTS', 'Permite ver reportes de ventas y desempeño', 'REPORTS', NOW());

INSERT INTO admin.roles (id, name, description, is_system, created_at) VALUES 
('e1111111-1111-1111-1111-111111111111', 'ADMIN', 'Administrador con acceso total a la organización', true, NOW()),
('e2222222-2222-2222-2222-222222222222', 'WAITER', 'Mesero con acceso a pedidos y mesas', true, NOW());

INSERT INTO admin.role_permissions (role_id, permission_id) 
SELECT 'e1111111-1111-1111-1111-111111111111', id FROM admin.permissions;

-- SEEDS: Test Developer User (jcasve141@gmail.com / CodlixSaaS2026)
INSERT INTO admin.users (id, email, password_hash, full_name, dni, phone_number, super_admin, active, created_at) VALUES 
('11eb3d14-2c69-48b4-afa7-9e96118c7956', 'jcasve141@gmail.com', crypt('CodlixSaaS2026', gen_salt('bf', 10)), 'User Admin', '12345678', '987654321', true, true, NOW());

-- SEEDS: Test Organization
INSERT INTO admin.organizations (id, name, slug, schema_name, owner_id, active, created_at) VALUES 
('605241c3-3551-4636-91e5-c743b521e082', 'Codlix Restaurant', 'codlix-restaurant', 'codlix', '11eb3d14-2c69-48b4-afa7-9e96118c7956', true, NOW());

INSERT INTO admin.organization_members (id, organization_id, user_id, role_id, status, joined_at, created_at) VALUES 
(uuid_generate_v4(), '605241c3-3551-4636-91e5-c743b521e082', '11eb3d14-2c69-48b4-afa7-9e96118c7956', 'e1111111-1111-1111-1111-111111111111', 'ACTIVE', NOW(), NOW());

-- SEEDS: Subscription Plans
INSERT INTO admin.subscription_plans (id, name, description, max_tables, max_users, price_monthly, active, created_at) VALUES
('a1111111-1111-1111-1111-111111111111', 'Free',    'Plan gratuito con funcionalidades básicas',  5,  3,   0.00, true, NOW()),
('a2222222-2222-2222-2222-222222222222', 'Básico',  'Plan básico para pequeños restaurantes',    10, 10,  49.90, true, NOW()),
('a3333333-3333-3333-3333-333333333333', 'Premium', 'Plan completo con todas las funciones',     50, 50, 149.90, true, NOW());

-- SEEDS: Organization Subscription (Codlix Restaurant → Plan Básico, 1 año, 10 usuarios)
INSERT INTO admin.organization_subscriptions (id, organization_id, plan_id, status, start_date, end_date, user_limit, created_at) VALUES
(uuid_generate_v4(),
 '605241c3-3551-4636-91e5-c743b521e082',
 'a2222222-2222-2222-2222-222222222222',
 'ACTIVE',
 CURRENT_DATE,
 CURRENT_DATE + INTERVAL '1 year',
 10,
 NOW());

-- SEEDS: Invitation Codes
INSERT INTO admin.invitation_codes (id, code, plan_name, trial_days)
VALUES 
    (uuid_generate_v4(), 'RT-FREE-2026', 'Free', 30),
    (uuid_generate_v4(), 'RT-PRO-99', 'Premium', 30);
