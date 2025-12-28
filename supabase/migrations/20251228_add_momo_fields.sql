-- Migration: Add MOMO fields to device_keys and insert default device
-- Run this on your Supabase database

-- Add new columns to device_keys table
ALTER TABLE device_keys ADD COLUMN IF NOT EXISTS momo_msisdn TEXT UNIQUE;
ALTER TABLE device_keys ADD COLUMN IF NOT EXISTS momo_code TEXT;
ALTER TABLE device_keys ADD COLUMN IF NOT EXISTS device_secret TEXT;
ALTER TABLE device_keys ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE device_keys ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

-- Create index for MOMO lookup
CREATE INDEX IF NOT EXISTS idx_device_keys_momo_msisdn ON device_keys(momo_msisdn);

-- Insert default device (update if exists)
INSERT INTO device_keys (device_id, device_label, secret_hash, device_secret, enabled, momo_msisdn)
VALUES (
  'de75ff85a17ee8a2d751a9d591c6680b',
  'SMSGateway-1',
  '7a9f8b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a', -- SHA256 hash
  '8dc5bb543edaed28b9389f687c2f9ea849702ff94f6b805eae63fbd20edd0bbc',
  true,
  '0788767816'
) ON CONFLICT (device_id) DO UPDATE SET 
  momo_msisdn = EXCLUDED.momo_msisdn,
  device_secret = EXCLUDED.device_secret,
  updated_at = NOW();

-- Create RLS policy for device lookup (anon can read their device by momo_msisdn)
ALTER TABLE device_keys ENABLE ROW LEVEL SECURITY;

-- Policy: Allow reading device info by MOMO number (for app lookup)
DROP POLICY IF EXISTS "Allow device lookup by momo" ON device_keys;
CREATE POLICY "Allow device lookup by momo" ON device_keys
  FOR SELECT
  USING (true); -- Allow all reads for now, adjust as needed

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for updated_at
DROP TRIGGER IF EXISTS update_device_keys_updated_at ON device_keys;
CREATE TRIGGER update_device_keys_updated_at
  BEFORE UPDATE ON device_keys
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();
