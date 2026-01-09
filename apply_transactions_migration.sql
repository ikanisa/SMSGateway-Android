-- Migration: Create transactions table to store MoMo payment transactions
-- Date: 2025-01-29
-- Links parsed MTN MoMo payment SMS to transaction records
-- 
-- IMPORTANT: Run this SQL in Supabase Dashboard SQL Editor:
-- https://supabase.com/dashboard/project/wadhydemushqqtcrrlwm/sql/new

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  sms_message_id UUID NOT NULL REFERENCES sms_messages(id) ON DELETE CASCADE,
  
  -- Transaction identification
  txn_id TEXT,  -- Transaction ID from SMS (if available)
  ft_id TEXT,   -- FT (Fund Transfer) ID from SMS (if available)
  
  -- Transaction details
  txn_type TEXT NOT NULL,  -- 'credit/received', 'debit/sent', 'cashout', 'payment', 'fee', 'unknown'
  amount NUMERIC(10, 2) NOT NULL,
  currency TEXT NOT NULL DEFAULT 'RWF',
  fee NUMERIC(10, 2),
  fee_currency TEXT,
  balance NUMERIC(10, 2),  -- Balance after transaction
  
  -- Counterparty information
  counterparty TEXT,
  counterparty_phone_suffix TEXT,  -- Last digits if masked (e.g. 235 from *****235)
  
  -- Transaction metadata
  reference TEXT,
  wallet TEXT,
  provider TEXT,  -- 'MTN MoMo', 'Airtel Money', etc.
  transaction_time TIMESTAMP WITH TIME ZONE,  -- Parsed transaction time
  transaction_time_raw TEXT,  -- Original timestamp text from SMS
  
  -- Status
  status TEXT DEFAULT 'pending',  -- 'pending', 'confirmed', 'failed', 'cancelled'
  
  -- Device/Account info
  momo_code TEXT,
  device_id UUID,
  
  -- Timestamps
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes for efficient queries
CREATE INDEX IF NOT EXISTS idx_transactions_sms_message_id ON transactions(sms_message_id);
CREATE INDEX IF NOT EXISTS idx_transactions_txn_id ON transactions(txn_id) WHERE txn_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_transactions_ft_id ON transactions(ft_id) WHERE ft_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_transactions_txn_type ON transactions(txn_type);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_momo_code ON transactions(momo_code) WHERE momo_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_transactions_transaction_time ON transactions(transaction_time DESC);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at DESC);

-- Unique constraint to prevent duplicate transactions from same SMS
CREATE UNIQUE INDEX IF NOT EXISTS idx_transactions_sms_message_unique 
ON transactions(sms_message_id);

-- Composite index for common queries
CREATE INDEX IF NOT EXISTS idx_transactions_momo_code_txn_type 
ON transactions(momo_code, txn_type) 
WHERE momo_code IS NOT NULL;

-- Enable Row Level Security
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

-- RLS Policies
-- Policy: Service role can do everything (for edge functions)
DROP POLICY IF EXISTS "Service role full access on transactions" ON transactions;
CREATE POLICY "Service role full access on transactions" ON transactions
  FOR ALL
  USING (auth.role() = 'service_role');

-- Policy: Authenticated users can read transactions
DROP POLICY IF EXISTS "Authenticated users can read transactions" ON transactions;
CREATE POLICY "Authenticated users can read transactions" ON transactions
  FOR SELECT
  USING (auth.role() = 'authenticated');

-- Policy: Anon users can read transactions (for app access with anon key)
DROP POLICY IF EXISTS "Anon users can read transactions" ON transactions;
CREATE POLICY "Anon users can read transactions" ON transactions
  FOR SELECT
  USING (true);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_transactions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for updated_at
DROP TRIGGER IF EXISTS update_transactions_updated_at_trigger ON transactions;
CREATE TRIGGER update_transactions_updated_at_trigger
  BEFORE UPDATE ON transactions
  FOR EACH ROW
  EXECUTE FUNCTION update_transactions_updated_at();

-- Add comment
COMMENT ON TABLE transactions IS 'Stores MTN MoMo payment transactions parsed from SMS messages';
COMMENT ON COLUMN transactions.sms_message_id IS 'Foreign key to sms_messages table - links transaction to source SMS';
COMMENT ON COLUMN transactions.txn_type IS 'Type: credit/received, debit/sent, cashout, payment, fee, unknown';
COMMENT ON COLUMN transactions.status IS 'Transaction status: pending, confirmed, failed, cancelled';

-- Verify table was created
SELECT 'Transactions table created successfully!' as status;
