-- Migration: Consolidate all SMS/MoMo related tables into single "transactions" table
-- Date: 2025-01-29
-- 
-- This migration consolidates all SMS/MoMo related tables into the transactions table.
-- The transactions table will have ONE column for raw MoMo SMS (raw_sms_text).
-- All parsing and processing is done via functions/logic after SMS is inserted.

-- Tables to consolidate and drop:
-- - momo_sms_raw → data goes into transactions.raw_sms_text
-- - sms_parse_attempts → parse_attempts, parse_error columns
-- - transaction_allocations → already exists in transactions
-- - momo_parsed_transactions → parsed data already in transactions
-- - vw_transactions_enriched, vw_transactions_consolidated → views to drop

-- Step 1: Make transaction fields nullable so raw SMS can be inserted without parsed data
ALTER TABLE transactions
  ALTER COLUMN type DROP NOT NULL,
  ALTER COLUMN amount DROP NOT NULL,
  ALTER COLUMN currency DROP NOT NULL,
  ALTER COLUMN channel DROP NOT NULL;

-- Step 2: Add missing columns from momo_sms_raw and other tables
ALTER TABLE transactions
  -- Raw SMS column (ONE column for raw MoMo SMS as requested)
  ADD COLUMN IF NOT EXISTS raw_sms_text TEXT,
  
  -- SMS metadata from momo_sms_raw
  ADD COLUMN IF NOT EXISTS sms_hash TEXT, -- For deduplication (make unique after data migration)
  ADD COLUMN IF NOT EXISTS message_hash TEXT,
  ADD COLUMN IF NOT EXISTS device_id UUID,
  ADD COLUMN IF NOT EXISTS momo_code TEXT,
  
  -- Parse status and attempts  
  ADD COLUMN IF NOT EXISTS parse_status TEXT DEFAULT 'pending',
  ADD COLUMN IF NOT EXISTS parse_error TEXT,
  ADD COLUMN IF NOT EXISTS parse_attempts INTEGER DEFAULT 0,
  
  -- Resolution fields (from momo_sms_raw)
  ADD COLUMN IF NOT EXISTS resolution_status TEXT,
  ADD COLUMN IF NOT EXISTS resolution_note TEXT,
  ADD COLUMN IF NOT EXISTS resolved_by UUID,
  ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP WITH TIME ZONE,
  
  -- Additional metadata
  ADD COLUMN IF NOT EXISTS meta JSONB,
  ADD COLUMN IF NOT EXISTS ingested_at TIMESTAMP WITH TIME ZONE;

-- Step 3: Map sender_phone (from momo_sms_raw) to payer_phone (existing in transactions)
-- They serve the same purpose, no new column needed

-- Step 4: Map received_at (from momo_sms_raw) to occurred_at (existing in transactions) 
-- If occurred_at is NULL and received_at exists, we'll update it

-- Step 5: Create unique constraint on sms_hash after adding it
DO $$
BEGIN
  -- Add unique constraint if not exists
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint 
    WHERE conname = 'transactions_sms_hash_unique'
  ) THEN
    CREATE UNIQUE INDEX transactions_sms_hash_unique ON transactions(sms_hash) 
    WHERE sms_hash IS NOT NULL;
  END IF;
END $$;

-- Step 6: Create comprehensive indexes for new columns
CREATE INDEX IF NOT EXISTS idx_transactions_raw_sms_hash ON transactions(sms_hash) WHERE sms_hash IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_transactions_message_hash ON transactions(message_hash) WHERE message_hash IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_transactions_payer_phone ON transactions(payer_phone) WHERE payer_phone IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_transactions_occurred_at ON transactions(occurred_at DESC) WHERE occurred_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_transactions_parse_status ON transactions(parse_status) WHERE parse_status IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_transactions_momo_code ON transactions(momo_code) WHERE momo_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_transactions_device_id ON transactions(device_id) WHERE device_id IS NOT NULL;

-- Step 7: Migrate data from momo_sms_raw to transactions
-- Insert raw SMS data into transactions table
INSERT INTO transactions (
  id,
  institution_id,
  raw_sms_text,
  payer_phone, -- Map sender_phone to payer_phone (existing column)
  occurred_at, -- Map received_at to occurred_at (existing column)
  received_at, -- Keep received_at for reference (new column)
  sms_hash,
  message_hash,
  momo_sms_id, -- Use existing momo_sms_id column to reference original
  momo_code,
  device_id,
  parse_status,
  parse_error,
  meta,
  ingested_at,
  created_at,
  channel -- Default channel for raw SMS (existing column)
)
SELECT 
  id,
  institution_id,
  COALESCE(sms_text, body) as raw_sms_text,
  COALESCE(sender_phone, sender) as payer_phone,
  received_at as occurred_at,
  received_at,
  COALESCE(sms_hash, message_hash) as sms_hash,
  message_hash,
  id as momo_sms_id, -- Reference to original SMS (existing column)
  momo_code,
  device_id,
  parse_status::text as parse_status,
  parse_error,
  meta,
  ingested_at,
  created_at,
  'momo' as channel -- Default channel
FROM momo_sms_raw
WHERE NOT EXISTS (
  SELECT 1 FROM transactions t 
  WHERE t.sms_hash = COALESCE(momo_sms_raw.sms_hash, momo_sms_raw.message_hash)
    OR t.momo_sms_id = momo_sms_raw.id
)
ON CONFLICT DO NOTHING;

-- Step 8: Update existing transactions with raw SMS data if they link via momo_sms_id
UPDATE transactions t
SET
  raw_sms_text = COALESCE(t.raw_sms_text, msr.sms_text, msr.body),
  payer_phone = COALESCE(t.payer_phone, msr.sender_phone, msr.sender),
  occurred_at = COALESCE(t.occurred_at, msr.received_at),
  received_at = COALESCE(t.received_at, msr.received_at),
  sms_hash = COALESCE(t.sms_hash, msr.sms_hash, msr.message_hash),
  message_hash = COALESCE(t.message_hash, msr.message_hash),
  parse_status = COALESCE(t.parse_status, msr.parse_status::text, 'pending'),
  parse_error = COALESCE(t.parse_error, msr.parse_error),
  meta = COALESCE(t.meta, msr.meta),
  ingested_at = COALESCE(t.ingested_at, msr.ingested_at),
  momo_code = COALESCE(t.momo_code, msr.momo_code),
  device_id = COALESCE(t.device_id, msr.device_id)
FROM momo_sms_raw msr
WHERE t.momo_sms_id = msr.id
  AND t.raw_sms_text IS NULL;

-- Step 9: Migrate parse attempts data
UPDATE transactions t
SET
  parse_attempts = (
    SELECT COUNT(*) 
    FROM sms_parse_attempts spa 
    WHERE spa.sms_id = t.id 
       OR spa.sms_id::text = t.sms_hash
       OR EXISTS (SELECT 1 FROM momo_sms_raw msr WHERE msr.id = spa.sms_id AND msr.sms_hash = t.sms_hash)
  ),
  parse_error = COALESCE(
    t.parse_error,
    (SELECT parse_error 
     FROM sms_parse_attempts spa 
     WHERE spa.sms_id = t.id 
        OR spa.sms_id::text = t.sms_hash
        OR EXISTS (SELECT 1 FROM momo_sms_raw msr WHERE msr.id = spa.sms_id AND msr.sms_hash = t.sms_hash)
     ORDER BY attempted_at DESC 
     LIMIT 1)
  )
WHERE EXISTS (
  SELECT 1 FROM sms_parse_attempts spa 
  WHERE spa.sms_id = t.id 
     OR spa.sms_id::text = t.sms_hash
     OR EXISTS (SELECT 1 FROM momo_sms_raw msr WHERE msr.id = spa.sms_id AND msr.sms_hash = t.sms_hash)
);

-- Step 10: Drop old views first (before dropping tables they depend on)
DROP VIEW IF EXISTS vw_transactions_enriched CASCADE;
DROP VIEW IF EXISTS vw_transactions_consolidated CASCADE;

-- Step 11: Drop old tables after data migration
DROP TABLE IF EXISTS momo_parsed_transactions CASCADE;
DROP TABLE IF EXISTS transaction_allocations CASCADE; -- Already consolidated into transactions
DROP TABLE IF EXISTS sms_parse_attempts CASCADE; -- Data migrated to parse_attempts column
DROP TABLE IF EXISTS momo_sms_raw CASCADE; -- Data migrated to transactions.raw_sms_text

-- Note: sms_sources might be used for configuration/reference - keep it if it's just lookup data
-- DROP TABLE IF EXISTS sms_sources CASCADE;

-- Step 12: Update RLS policies (should already exist, but ensure they're correct)
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Service role full access on transactions" ON transactions;
CREATE POLICY "Service role full access on transactions" ON transactions
  FOR ALL USING (auth.role() = 'service_role');

DROP POLICY IF EXISTS "Authenticated users can read transactions" ON transactions;
CREATE POLICY "Authenticated users can read transactions" ON transactions
  FOR SELECT USING (auth.role() = 'authenticated');

DROP POLICY IF EXISTS "Anon users can read transactions" ON transactions;
CREATE POLICY "Anon users can read transactions" ON transactions
  FOR SELECT USING (true);

-- Step 13: Ensure trigger exists for updated_at
CREATE OR REPLACE FUNCTION update_transactions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_transactions_updated_at_trigger ON transactions;
CREATE TRIGGER update_transactions_updated_at_trigger
  BEFORE UPDATE ON transactions
  FOR EACH ROW
  EXECUTE FUNCTION update_transactions_updated_at();

-- Step 14: Add helpful comments
COMMENT ON TABLE transactions IS 'Consolidated transactions table - contains raw SMS (raw_sms_text), parsed data, and allocations all in one place. SMS gateway feeds directly into this table.';
COMMENT ON COLUMN transactions.raw_sms_text IS 'Raw MoMo SMS text - single column for raw SMS as requested. SMS gateway inserts SMS here.';
COMMENT ON COLUMN transactions.sms_hash IS 'SHA256 hash of SMS for deduplication - unique constraint ensures no duplicate SMS';
COMMENT ON COLUMN transactions.parse_status IS 'SMS parsing status: pending, parsed, failed';
COMMENT ON COLUMN transactions.parse_attempts IS 'Number of parse attempts made';
COMMENT ON COLUMN transactions.momo_code IS 'MoMo code identifier for the device/account';
