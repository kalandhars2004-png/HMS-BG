-- Enhance alerts table for real notification system — branch/user scoping, deduplication, metadata
-- Keeps existing table, adds new columns idempotently

ALTER TABLE alerts ADD COLUMN IF NOT EXISTS branch_id BIGINT REFERENCES branches(id);
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id);
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS read_at TIMESTAMP;
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS is_resolved BOOLEAN DEFAULT FALSE;
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP;
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS metadata TEXT;
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();

-- Rename read column to read_flag if needed? Keep both for compat — add read_flag and migrate
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS read_flag BOOLEAN DEFAULT FALSE;
UPDATE alerts SET read_flag = read WHERE read_flag IS NULL AND read IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_alerts_branch ON alerts(branch_id);
CREATE INDEX IF NOT EXISTS idx_alerts_user_read ON alerts(user_id, read_flag);
CREATE INDEX IF NOT EXISTS idx_alerts_created ON alerts(created_at);
CREATE INDEX IF NOT EXISTS idx_alerts_type ON alerts(type);
CREATE INDEX IF NOT EXISTS idx_alerts_branch_type_resolved ON alerts(branch_id, type, is_resolved);
