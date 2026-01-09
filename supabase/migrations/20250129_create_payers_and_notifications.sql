-- Migration: Create payers and notifications tables for SMS notification system
-- Date: 2025-01-29
-- Run this on your Supabase database

-- Create payers table
CREATE TABLE IF NOT EXISTS payers (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  phone_number TEXT NOT NULL UNIQUE,
  name TEXT,
  balance NUMERIC(10, 2) DEFAULT 0.0,
  currency TEXT DEFAULT 'RWF',
  is_active BOOLEAN DEFAULT true,
  group_id UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create index for phone number lookups
CREATE INDEX IF NOT EXISTS idx_payers_phone_number ON payers(phone_number);
CREATE INDEX IF NOT EXISTS idx_payers_active ON payers(is_active) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_payers_group ON payers(group_id) WHERE group_id IS NOT NULL;

-- Create notifications log table (optional but recommended)
CREATE TABLE IF NOT EXISTS notifications (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  type TEXT NOT NULL,
  message TEXT NOT NULL,
  recipient_count INTEGER DEFAULT 0,
  sent_count INTEGER DEFAULT 0,
  failed_count INTEGER DEFAULT 0,
  parameters JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create index for notifications queries
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);

-- Create function to update updated_at timestamp for payers
CREATE OR REPLACE FUNCTION update_payers_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for updated_at on payers
DROP TRIGGER IF EXISTS update_payers_updated_at_trigger ON payers;
CREATE TRIGGER update_payers_updated_at_trigger
  BEFORE UPDATE ON payers
  FOR EACH ROW
  EXECUTE FUNCTION update_payers_updated_at();

-- Enable Row Level Security
ALTER TABLE payers ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- RLS Policies for payers table
-- Policy: Service role can do everything (for edge functions)
DROP POLICY IF EXISTS "Service role full access on payers" ON payers;
CREATE POLICY "Service role full access on payers" ON payers
  FOR ALL
  USING (auth.role() = 'service_role');

-- Policy: Authenticated users can read active payers
DROP POLICY IF EXISTS "Authenticated users can read active payers" ON payers;
CREATE POLICY "Authenticated users can read active payers" ON payers
  FOR SELECT
  USING (auth.role() = 'authenticated' AND is_active = true);

-- Policy: Anon users can read active payers (for app access with anon key)
DROP POLICY IF EXISTS "Anon users can read active payers" ON payers;
CREATE POLICY "Anon users can read active payers" ON payers
  FOR SELECT
  USING (is_active = true);

-- RLS Policies for notifications table
-- Policy: Service role can insert notifications
DROP POLICY IF EXISTS "Service role can insert notifications" ON notifications;
CREATE POLICY "Service role can insert notifications" ON notifications
  FOR INSERT
  WITH CHECK (auth.role() = 'service_role');

-- Policy: Authenticated users can read notifications
DROP POLICY IF EXISTS "Authenticated users can read notifications" ON notifications;
CREATE POLICY "Authenticated users can read notifications" ON notifications
  FOR SELECT
  USING (auth.role() = 'authenticated');

-- Policy: Anon users can read notifications (for app access)
DROP POLICY IF EXISTS "Anon users can read notifications" ON notifications;
CREATE POLICY "Anon users can read notifications" ON notifications
  FOR SELECT
  USING (true);

-- Insert sample payers (optional - remove if not needed)
-- Uncomment and modify as needed:
/*
INSERT INTO payers (phone_number, name, balance, currency, is_active)
VALUES
  ('+250788123456', 'John Doe', 5000.00, 'RWF', true),
  ('+250788789012', 'Jane Smith', 7500.00, 'RWF', true),
  ('+250788345678', 'Bob Johnson', 3000.00, 'RWF', true)
ON CONFLICT (phone_number) DO NOTHING;
*/

-- Add comment to tables
COMMENT ON TABLE payers IS 'Table storing payer/member information for SMS notifications';
COMMENT ON TABLE notifications IS 'Log table for tracking sent notifications';
