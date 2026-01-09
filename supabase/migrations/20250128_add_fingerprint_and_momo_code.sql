-- Migration: Add fingerprint for deduplication and momo_code support
-- This migration updates the schema to support the simplified device identification
-- and server-side deduplication using SHA256 fingerprints.

-- 1. Add fingerprint column to sms_messages for deduplication
ALTER TABLE sms_messages
ADD COLUMN IF NOT EXISTS fingerprint TEXT;

-- 2. Create unique index on fingerprint to prevent duplicates
CREATE UNIQUE INDEX IF NOT EXISTS idx_sms_messages_fingerprint 
ON sms_messages(fingerprint) 
WHERE fingerprint IS NOT NULL;

-- 3. Add momo_code column to sms_messages (if not exists)
ALTER TABLE sms_messages
ADD COLUMN IF NOT EXISTS momo_code TEXT;

-- 4. Create index on momo_code for faster lookups
CREATE INDEX IF NOT EXISTS idx_sms_messages_momo_code 
ON sms_messages(momo_code);

-- 5. Create devices table if it doesn't exist (for momo_code lookup)
CREATE TABLE IF NOT EXISTS devices (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  momo_code TEXT UNIQUE NOT NULL,
  device_label TEXT,
  enabled BOOLEAN DEFAULT true,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. Create index on devices.momo_code
CREATE INDEX IF NOT EXISTS idx_devices_momo_code 
ON devices(momo_code);

-- 7. Optional: Create unknown_senders table for logging unrecognized senders
CREATE TABLE IF NOT EXISTS unknown_senders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  sender TEXT NOT NULL,
  body TEXT,
  momo_code TEXT,
  received_at TIMESTAMPTZ DEFAULT NOW(),
  reviewed BOOLEAN DEFAULT false,
  added_to_allowlist BOOLEAN DEFAULT false
);

-- 8. Create index on unknown_senders for review workflow
CREATE INDEX IF NOT EXISTS idx_unknown_senders_reviewed 
ON unknown_senders(reviewed, received_at DESC);

-- Note: If you have existing device_keys table, you may want to migrate data:
-- INSERT INTO devices (momo_code, device_label, enabled)
-- SELECT DISTINCT momo_code, device_label, true
-- FROM device_keys
-- WHERE momo_code IS NOT NULL
-- ON CONFLICT (momo_code) DO NOTHING;
