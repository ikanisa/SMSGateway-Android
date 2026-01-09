-- Migration: Merge sms_sources and institution_momo_codes into institutions table
-- Date: 2025-01-29
-- 
-- This migration:
-- 1. Adds SMS source configuration columns to institutions table
-- 2. Merges institution_momo_codes data (if institutions can have multiple MoMo codes, use JSONB array)
-- 3. Drops sms_sources and institution_momo_codes tables

-- Step 1: Add SMS source configuration columns to institutions
ALTER TABLE institutions
  -- SMS Source configuration (from sms_sources)
  ADD COLUMN IF NOT EXISTS sms_source_name TEXT,
  ADD COLUMN IF NOT EXISTS sms_source_type TEXT DEFAULT 'android_gateway',
  ADD COLUMN IF NOT EXISTS sms_device_identifier TEXT,
  ADD COLUMN IF NOT EXISTS sms_webhook_secret TEXT,
  ADD COLUMN IF NOT EXISTS sms_source_active BOOLEAN DEFAULT true,
  ADD COLUMN IF NOT EXISTS sms_last_seen_at TIMESTAMP WITH TIME ZONE,
  ADD COLUMN IF NOT EXISTS sms_message_count INTEGER DEFAULT 0,
  
  -- Additional MoMo codes (from institution_momo_codes - for multiple codes per institution)
  ADD COLUMN IF NOT EXISTS additional_momo_codes JSONB DEFAULT '[]'::jsonb,
  ADD COLUMN IF NOT EXISTS primary_momo_code TEXT; -- Primary/default MoMo code

-- Step 2: Migrate institution_momo_codes data
-- Since institutions already has momo_code, we'll:
-- - Set primary_momo_code from institution_momo_codes where is_primary = true
-- - Store all momo codes (including existing) in additional_momo_codes JSONB array
UPDATE institutions i
SET
  primary_momo_code = COALESCE(
    (SELECT momo_code FROM institution_momo_codes imc 
     WHERE imc.institution_id = i.id AND imc.is_primary = true 
     LIMIT 1),
    i.momo_code -- Fallback to existing momo_code
  ),
  additional_momo_codes = COALESCE(
    (SELECT jsonb_agg(jsonb_build_object(
      'momo_code', momo_code,
      'is_active', is_active,
      'is_primary', is_primary,
      'created_at', created_at
    ))
    FROM institution_momo_codes imc
    WHERE imc.institution_id = i.id),
    CASE 
      WHEN i.momo_code IS NOT NULL THEN 
        jsonb_build_array(jsonb_build_object(
          'momo_code', i.momo_code,
          'is_active', true,
          'is_primary', true
        ))
      ELSE '[]'::jsonb
    END
  )
WHERE EXISTS (
  SELECT 1 FROM institution_momo_codes imc WHERE imc.institution_id = i.id
)
OR i.momo_code IS NOT NULL;

-- Step 3: Migrate sms_sources data to institutions
-- Update institutions with SMS source configuration from sms_sources
UPDATE institutions i
SET
  sms_source_name = ss.name,
  sms_source_type = ss.source_type,
  sms_device_identifier = ss.device_identifier,
  sms_webhook_secret = ss.webhook_secret,
  sms_source_active = ss.is_active,
  sms_last_seen_at = ss.last_seen_at,
  sms_message_count = ss.message_count
FROM sms_sources ss
WHERE ss.institution_id = i.id;

-- Step 4: If there are sms_sources without matching institution_id, create placeholder or handle
-- (For now, we'll leave orphaned sms_sources - they'll be dropped with the table)

-- Step 5: Create indexes for new columns
CREATE INDEX IF NOT EXISTS idx_institutions_sms_device_identifier 
ON institutions(sms_device_identifier) 
WHERE sms_device_identifier IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_institutions_primary_momo_code 
ON institutions(primary_momo_code) 
WHERE primary_momo_code IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_institutions_sms_source_active 
ON institutions(sms_source_active) 
WHERE sms_source_active = true;

-- Step 6: Update momo_code to use primary_momo_code if primary_momo_code is set
UPDATE institutions
SET momo_code = primary_momo_code
WHERE primary_momo_code IS NOT NULL 
AND (momo_code IS NULL OR momo_code != primary_momo_code);

-- Step 7: Drop old tables
DROP TABLE IF EXISTS sms_sources CASCADE;
DROP TABLE IF EXISTS institution_momo_codes CASCADE;

-- Step 8: Add comments
COMMENT ON COLUMN institutions.momo_code IS 'Primary/default MoMo code for this institution';
COMMENT ON COLUMN institutions.primary_momo_code IS 'Primary MoMo code (same as momo_code, kept for clarity)';
COMMENT ON COLUMN institutions.additional_momo_codes IS 'JSONB array of additional MoMo codes: [{"momo_code": "...", "is_active": true, "is_primary": false}]';
COMMENT ON COLUMN institutions.sms_source_name IS 'Name of the SMS source/device (e.g., "Office Phone Samsung A52")';
COMMENT ON COLUMN institutions.sms_source_type IS 'Type of SMS source: android_gateway, webhook, etc.';
COMMENT ON COLUMN institutions.sms_device_identifier IS 'Device identifier for Android gateway sources';
COMMENT ON COLUMN institutions.sms_webhook_secret IS 'Webhook secret for webhook sources';
