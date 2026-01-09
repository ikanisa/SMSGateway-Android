-- Migration: Fix sms_sources merge - should be in transactions, not institutions
-- Date: 2025-01-29
-- 
-- This migration:
-- 1. Removes SMS source columns from institutions (wrong place)
-- 2. Adds SMS source columns to transactions table (correct place)
-- 3. Migrates SMS source data from institutions to transactions
-- 4. Drops SMS source columns from institutions

-- Step 1: Add SMS source columns to transactions table (where they belong)
ALTER TABLE transactions
  -- SMS Source configuration (from sms_sources - should be in transactions)
  ADD COLUMN IF NOT EXISTS sms_source_name TEXT,
  ADD COLUMN IF NOT EXISTS sms_source_type TEXT DEFAULT 'android_gateway',
  ADD COLUMN IF NOT EXISTS sms_device_identifier TEXT,
  ADD COLUMN IF NOT EXISTS sms_webhook_secret TEXT,
  ADD COLUMN IF NOT EXISTS sms_source_active BOOLEAN DEFAULT true,
  ADD COLUMN IF NOT EXISTS sms_last_seen_at TIMESTAMP WITH TIME ZONE,
  ADD COLUMN IF NOT EXISTS sms_message_count INTEGER DEFAULT 0;

-- Step 2: Migrate SMS source data from institutions to transactions
-- Match by institution_id to link transactions to their SMS source config
UPDATE transactions t
SET
  sms_source_name = i.sms_source_name,
  sms_source_type = i.sms_source_type,
  sms_device_identifier = i.sms_device_identifier,
  sms_webhook_secret = i.sms_webhook_secret,
  sms_source_active = i.sms_source_active,
  sms_last_seen_at = i.sms_last_seen_at,
  sms_message_count = i.sms_message_count
FROM institutions i
WHERE t.institution_id = i.id
  AND i.sms_source_name IS NOT NULL
  AND t.sms_source_name IS NULL;

-- Step 3: Create indexes for SMS source columns in transactions
CREATE INDEX IF NOT EXISTS idx_transactions_sms_device_identifier 
ON transactions(sms_device_identifier) 
WHERE sms_device_identifier IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_transactions_sms_source_type 
ON transactions(sms_source_type);

CREATE INDEX IF NOT EXISTS idx_transactions_sms_source_active 
ON transactions(sms_source_active) 
WHERE sms_source_active = true;

-- Step 4: Remove SMS source columns from institutions (wrong place)
ALTER TABLE institutions
  DROP COLUMN IF EXISTS sms_source_name,
  DROP COLUMN IF EXISTS sms_source_type,
  DROP COLUMN IF EXISTS sms_device_identifier,
  DROP COLUMN IF EXISTS sms_webhook_secret,
  DROP COLUMN IF EXISTS sms_source_active,
  DROP COLUMN IF EXISTS sms_last_seen_at,
  DROP COLUMN IF EXISTS sms_message_count;

-- Step 5: Drop indexes from institutions that we just removed
DROP INDEX IF EXISTS idx_institutions_sms_device_identifier;
DROP INDEX IF EXISTS idx_institutions_sms_source_active;

-- Step 6: Add comments to transactions table
COMMENT ON COLUMN transactions.sms_source_name IS 'Name of the SMS source/device (e.g., "Office Phone Samsung A52") - from sms_sources table';
COMMENT ON COLUMN transactions.sms_source_type IS 'Type of SMS source: android_gateway, webhook, etc.';
COMMENT ON COLUMN transactions.sms_device_identifier IS 'Device identifier for Android gateway sources';
COMMENT ON COLUMN transactions.sms_webhook_secret IS 'Webhook secret for webhook sources';
COMMENT ON COLUMN transactions.sms_source_active IS 'Whether the SMS source is currently active';
COMMENT ON COLUMN transactions.sms_last_seen_at IS 'Last time this SMS source sent a transaction';
COMMENT ON COLUMN transactions.sms_message_count IS 'Total count of messages from this SMS source';
