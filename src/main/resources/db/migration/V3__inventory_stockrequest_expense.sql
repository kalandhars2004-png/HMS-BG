-- Phase 4-5: inventory, stock_requests, expenses (JPA also auto-creates, this ensures prod validate passes)

CREATE TABLE IF NOT EXISTS inventories (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    branch_id BIGINT NOT NULL REFERENCES branches(id),
    warehouse_id BIGINT REFERENCES warehouses(id),
    product_id BIGINT NOT NULL,
    batch_id BIGINT,
    quantity_on_hand INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    version BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_inventory_branch_warehouse_product_batch UNIQUE (branch_id, warehouse_id, product_id, batch_id)
);
CREATE INDEX IF NOT EXISTS idx_inv_branch ON inventories(branch_id);
CREATE INDEX IF NOT EXISTS idx_inv_branch_product ON inventories(branch_id, product_id);
CREATE INDEX IF NOT EXISTS idx_inv_branch_warehouse_product ON inventories(branch_id, warehouse_id, product_id);

CREATE TABLE IF NOT EXISTS stock_requests (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    branch_id BIGINT NOT NULL REFERENCES branches(id),
    source_branch_id BIGINT REFERENCES branches(id),
    source_warehouse_id BIGINT REFERENCES warehouses(id),
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    requested_by BIGINT REFERENCES users(id),
    approved_by BIGINT REFERENCES users(id),
    shipped_by BIGINT REFERENCES users(id),
    received_by BIGINT REFERENCES users(id),
    requested_at TIMESTAMP NOT NULL DEFAULT NOW(),
    approved_at TIMESTAMP,
    shipped_at TIMESTAMP,
    received_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_sr_branch ON stock_requests(branch_id);
CREATE INDEX IF NOT EXISTS idx_sr_status ON stock_requests(status);

CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    branch_id BIGINT NOT NULL REFERENCES branches(id),
    amount NUMERIC(19,2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_by BIGINT REFERENCES users(id),
    date TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_expenses_branch ON expenses(branch_id);
CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses(date);
