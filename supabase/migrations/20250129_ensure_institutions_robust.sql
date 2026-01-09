-- Migration: Ensure institutions table is robust with proper structure
-- Date: 2025-01-29
-- 
-- Institutions table should have all needed columns to link to:
-- - devices (via institution_id foreign key in devices/sms_gateway_devices)
-- - members (via institution_id foreign key in members)
-- - groups (via institution_id foreign key in groups)
-- - staff (via institution_id if staff table exists)
-- - transactions (via institution_id foreign key in transactions)
-- 
-- No need for SMS source configuration - there's only MTN MoMo

-- Step 1: Ensure institutions has all MoMo code columns (already have momo_code)
-- Add additional_momo_codes if not exists (for multiple codes per institution)
ALTER TABLE institutions
  ADD COLUMN IF NOT EXISTS additional_momo_codes JSONB DEFAULT '[]'::jsonb,
  ADD COLUMN IF NOT EXISTS primary_momo_code TEXT;

-- Step 2: Update primary_momo_code to match momo_code if it's NULL
UPDATE institutions
SET primary_momo_code = momo_code
WHERE primary_momo_code IS NULL AND momo_code IS NOT NULL;

-- Step 3: Ensure proper indexes exist for foreign key lookups
-- (Foreign keys are in child tables, but indexes help with reverse lookups)
CREATE INDEX IF NOT EXISTS idx_institutions_momo_code 
ON institutions(momo_code) 
WHERE momo_code IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_institutions_primary_momo_code 
ON institutions(primary_momo_code) 
WHERE primary_momo_code IS NOT NULL;

-- Step 4: Add comments
COMMENT ON TABLE institutions IS 'Institutions/SACCOs table - central table linking to devices, members, groups, staff, and transactions';
COMMENT ON COLUMN institutions.momo_code IS 'Primary MoMo code for this institution';
COMMENT ON COLUMN institutions.primary_momo_code IS 'Primary MoMo code (same as momo_code)';
COMMENT ON COLUMN institutions.additional_momo_codes IS 'JSONB array of additional MoMo codes: [{"momo_code": "...", "is_active": true, "is_primary": false}]';
