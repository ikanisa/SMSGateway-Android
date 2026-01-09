-- Migration: Remove unnecessary SMS source columns from transactions
-- Date: 2025-01-29
-- 
-- There is ONLY ONE SMS source: MTN MoMo
-- Remove: sms_source_name, sms_source_type, sms_device_identifier, sms_webhook_secret
-- Keep it simple - just raw SMS and parsed data

-- Step 1: Remove unnecessary SMS source configuration columns
ALTER TABLE transactions
  DROP COLUMN IF EXISTS sms_source_name,
  DROP COLUMN IF EXISTS sms_source_type,
  DROP COLUMN IF EXISTS sms_device_identifier,
  DROP COLUMN IF EXISTS sms_webhook_secret,
  DROP COLUMN IF EXISTS sms_source_active,
  DROP COLUMN IF EXISTS sms_last_seen_at,
  DROP COLUMN IF EXISTS sms_message_count;

-- Step 2: Remove related indexes
DROP INDEX IF EXISTS idx_transactions_sms_device_identifier;
DROP INDEX IF EXISTS idx_transactions_sms_source_type;
DROP INDEX IF EXISTS idx_transactions_sms_source_active;

-- Step 3: Simplify source - it's always MTN MoMo
-- Update source column to 'MTN MoMo' if it exists and has other values
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'transactions' AND column_name = 'source'
  ) THEN
    UPDATE transactions
    SET source = 'MTN MoMo'
    WHERE source IS NOT NULL AND source != 'MTN MoMo';
  END IF;
END $$;
