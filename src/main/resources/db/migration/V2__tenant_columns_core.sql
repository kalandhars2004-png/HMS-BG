-- Phase 3: tenant columns for core transactional tables (§6, §36)
-- All nullable first, backfilled to Main Branch (1), then NOT NULL for strict isolation in V3 if needed.
-- Keeping nullable for now to avoid breaking existing insert paths before service layer is migrated.

-- Products
ALTER TABLE products ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE products ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_products_branch ON products(branch_id);
CREATE INDEX IF NOT EXISTS idx_products_branch_sku ON products(branch_id, sku);
UPDATE products SET branch_id = COALESCE(warehouse_id, 0) * 0 + 1, organization_id = 1 WHERE branch_id IS NULL;
-- If warehouseId was set, map: treat as central warehouse branch 2 for those rows? Keep Main for now, refined later.

-- Batches (batch-aware §16)
ALTER TABLE batches ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE batches ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_batches_branch ON batches(branch_id);
CREATE INDEX IF NOT EXISTS idx_batches_branch_product ON batches(branch_id, product_id);
UPDATE batches SET branch_id = 1, organization_id = 1 WHERE branch_id IS NULL;

-- Customers (§24)
ALTER TABLE customers ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_customers_branch ON customers(branch_id);
UPDATE customers SET branch_id = 1, organization_id = 1 WHERE branch_id IS NULL;

-- Suppliers (§25) — suppliers can be company-wide, so nullable branch_id = global if null; we set 1 for existing
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_suppliers_branch ON suppliers(branch_id);
UPDATE suppliers SET branch_id = 1, organization_id = 1 WHERE branch_id IS NULL;

-- Transactions (§18, §15 ledger)
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_transactions_branch ON transactions(branch_id);
CREATE INDEX IF NOT EXISTS idx_transactions_branch_type_created ON transactions(branch_id, transaction_type, created_at);
UPDATE transactions SET branch_id = 1, organization_id = 1 WHERE branch_id IS NULL;

-- Sales orders
ALTER TABLE sales_orders ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE sales_orders ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_sales_orders_branch ON sales_orders(branch_id);
UPDATE sales_orders SET branch_id = 1, organization_id = 1 WHERE branch_id IS NULL;

-- Purchase orders
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_purchase_orders_branch ON purchase_orders(branch_id);
UPDATE purchase_orders SET branch_id = 1, organization_id = 1 WHERE branch_id IS NULL;

-- Invoices
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_invoices_branch ON invoices(branch_id);
UPDATE invoices SET branch_id = 1, organization_id = 1 WHERE branch_id IS NULL;

-- Stock movements (§15)
ALTER TABLE stock_movements ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE stock_movements ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_sm_branch ON stock_movements(branch_id);
CREATE INDEX IF NOT EXISTS idx_sm_branch_product ON stock_movements(branch_id, product_id);
UPDATE stock_movements SET branch_id = 1, organization_id = 1 WHERE branch_id IS NULL;

-- Inventory movements
ALTER TABLE inventory_movements ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_im_org ON inventory_movements(organization_id);
UPDATE inventory_movements SET organization_id = 1 WHERE organization_id IS NULL;
-- warehouse_id already exists, branch_id via warehouse

-- POS
ALTER TABLE pos_sessions ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE pos_sessions ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_pos_sessions_branch ON pos_sessions(branch_id);
UPDATE pos_sessions SET branch_id = 1, organization_id = 1 WHERE branch_id IS NULL;

ALTER TABLE pos_transactions ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE pos_transactions ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_pos_tx_branch ON pos_transactions(branch_id);
UPDATE pos_transactions SET branch_id = 1, organization_id = 1 WHERE branch_id IS NULL;

-- Stock transfers already have warehouse ids; add branch scoping
ALTER TABLE stock_transfers ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE stock_transfers ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_stock_transfers_branch ON stock_transfers(branch_id);
UPDATE stock_transfers SET branch_id = 1, organization_id = 1 WHERE branch_id IS NULL;
