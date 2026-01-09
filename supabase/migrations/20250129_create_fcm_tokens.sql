-- Migration: Create FCM tokens table for Firebase Cloud Messaging token storage
-- Date: 2025-01-29
-- Run this on your Supabase database

-- Create FCM tokens table
CREATE TABLE IF NOT EXISTS fcm_tokens (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  fcm_token TEXT NOT NULL UNIQUE,
  device_id TEXT,
  momo_code TEXT,
  user_id UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes for efficient queries
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_momo_code ON fcm_tokens(momo_code) WHERE momo_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_user_id ON fcm_tokens(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_device_id ON fcm_tokens(device_id) WHERE device_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_token ON fcm_tokens(fcm_token);

-- Enable Row Level Security
ALTER TABLE fcm_tokens ENABLE ROW LEVEL SECURITY;

-- RLS Policies
-- Policy: Service role can do everything (for edge functions)
DROP POLICY IF EXISTS "Service role full access on fcm_tokens" ON fcm_tokens;
CREATE POLICY "Service role full access on fcm_tokens" ON fcm_tokens
  FOR ALL
  USING (auth.role() = 'service_role');

-- Policy: Anon users can insert/update their own tokens (for app registration)
DROP POLICY IF EXISTS "Anon users can manage fcm tokens" ON fcm_tokens;
CREATE POLICY "Anon users can manage fcm tokens" ON fcm_tokens
  FOR ALL
  USING (true); -- Adjust based on your security requirements
  -- More restrictive example:
  -- USING (momo_code = current_setting('app.momo_code', true));

-- Policy: Authenticated users can read tokens (optional, for admin views)
DROP POLICY IF EXISTS "Authenticated users can read fcm tokens" ON fcm_tokens;
CREATE POLICY "Authenticated users can read fcm tokens" ON fcm_tokens
  FOR SELECT
  USING (auth.role() = 'authenticated');

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_fcm_tokens_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for updated_at
DROP TRIGGER IF EXISTS update_fcm_tokens_updated_at_trigger ON fcm_tokens;
CREATE TRIGGER update_fcm_tokens_updated_at_trigger
  BEFORE UPDATE ON fcm_tokens
  FOR EACH ROW
  EXECUTE FUNCTION update_fcm_tokens_updated_at();

-- Add comment
COMMENT ON TABLE fcm_tokens IS 'Stores Firebase Cloud Messaging tokens for push notification targeting';
