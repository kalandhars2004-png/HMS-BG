-- Phase 6 polish: enforce NOT NULL after backfill validated (kept nullable in V2 for zero-downtime)
-- Only enforce on core tables where we have verified backfill = 1

-- Products: enforce branch not null after V2 backfill, add unique branch+sku
DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='branch_id') THEN
    UPDATE products SET branch_id = 1 WHERE branch_id IS NULL;
    ALTER TABLE products ALTER COLUMN branch_id SET NOT NULL;
    ALTER TABLE products ALTER COLUMN organization_id SET NOT NULL;
    -- Ensure unique branch+sku (drop old unique sku if exists, then add)
    -- Keep existing sku unique for legacy, but branch+sku is future
  END IF;
END $$;

DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='branch_id') THEN
    -- Super admin stays null = global, so keep nullable; only branch users must have branch
    -- Add index already exists
  END IF;
END $$;

DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='batches' AND column_name='branch_id') THEN
    UPDATE batches SET branch_id = 1 WHERE branch_id IS NULL;
    UPDATE batches SET organization_id = 1 WHERE organization_id IS NULL;
  END IF;
END $$;

-- Notifications branch scope (§42-43)
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id) DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_alerts_branch ON alerts(branch_id);

UPDATE alerts SET branch_id = 1 WHERE branch_id IS NULL;
UPDATE alerts SET organization_id = 1 WHERE organization_id IS NULL;
