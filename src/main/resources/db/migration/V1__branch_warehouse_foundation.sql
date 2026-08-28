-- Phase 1: Branch + Organization foundation (non-breaking, nullable FKs for existing data)
-- Central warehouse concept: branch.type = CENTRAL_WAREHOUSE

CREATE TABLE IF NOT EXISTS organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    slug VARCHAR(255) UNIQUE,
    address VARCHAR(500),
    phone VARCHAR(50),
    email VARCHAR(255),
    tax_number VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO organizations (id, name, slug) VALUES (1, 'Default Organization', 'default-organization')
ON CONFLICT (id) DO NOTHING;
SELECT setval('organizations_id_seq', (SELECT MAX(id) FROM organizations));

CREATE TABLE IF NOT EXISTS branches (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'RETAIL',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    phone VARCHAR(50),
    email VARCHAR(255),
    tax_number VARCHAR(100),
    operating_hours VARCHAR(255),
    manager_id BIGINT,
    contact_person VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_branches_org ON branches(organization_id);
CREATE INDEX IF NOT EXISTS idx_branches_status ON branches(status);
CREATE INDEX IF NOT EXISTS idx_branches_code ON branches(code);

-- Seed default branches: Main Branch + Central Warehouse
INSERT INTO branches (id, organization_id, code, name, type, status, address, city, phone, email)
VALUES
    (1, 1, 'BR-MAIN', 'Main Branch', 'RETAIL', 'ACTIVE', 'Main Street', 'New York', '+1 000 000 0000', 'main@pharmacy.local'),
    (2, 1, 'WH-CENTRAL', 'Central Warehouse', 'CENTRAL_WAREHOUSE', 'ACTIVE', 'Industrial Zone', 'New York', '+1 000 000 0001', 'warehouse@pharmacy.local')
ON CONFLICT (id) DO NOTHING;
SELECT setval('branches_id_seq', (SELECT MAX(id) FROM branches));

-- Add branch_id to users (nullable for migration, SUPER_ADMIN stays null = global)
ALTER TABLE users ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE users ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_users_branch ON users(branch_id);

-- Backfill existing users to Main Branch (except we keep them on Main for now; super-admin to be promoted manually)
UPDATE users SET branch_id = 1, organization_id = 1 WHERE branch_id IS NULL;
-- Promote first ADMIN to SUPER_ADMIN for bootstrap
UPDATE users SET role = 'SUPER_ADMIN' WHERE id = (SELECT id FROM users ORDER BY id LIMIT 1) AND role = 'ADMIN';

-- Add branch_id to warehouses (nullable initially, then backfill)
ALTER TABLE warehouses ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE warehouses ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
ALTER TABLE warehouses ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();
CREATE INDEX IF NOT EXISTS idx_warehouses_branch ON warehouses(branch_id);

-- Backfill warehouses to Central Warehouse branch (type CENTRAL_WAREHOUSE)
UPDATE warehouses SET branch_id = 2 WHERE branch_id IS NULL;

-- Fix audit_logs: add branch/user scoping (nullable for old rows)
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);
CREATE INDEX IF NOT EXISTS idx_audit_branch ON audit_logs(branch_id);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);
