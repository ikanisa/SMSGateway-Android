# ✅ Merge Fix Complete - SMS Sources Now in Transactions

## Summary

Fixed the merge: `sms_sources` data is now correctly in the `transactions` table (not institutions).

## Correct Merge Locations

### ✅ `institution_momo_codes` → `institutions` (CORRECT)
- Status: ✅ Correctly merged into `institutions` table
- Columns: `primary_momo_code`, `additional_momo_codes` (JSONB)
- Status: DROPPED

### ✅ `sms_sources` → `transactions` (FIXED)
- Status: ✅ Now correctly merged into `transactions` table
- Columns: `sms_source_name`, `sms_source_type`, `sms_device_identifier`, etc.
- Status: DROPPED from institutions, added to transactions

## What Was Fixed

### 1. ✅ Removed SMS Source Columns from Institutions (Wrong Place)
- Dropped all `sms_*` columns from `institutions` table
- Removed related indexes

### 2. ✅ Added SMS Source Columns to Transactions (Correct Place)
- `sms_source_name` TEXT
- `sms_source_type` TEXT DEFAULT 'android_gateway'
- `sms_device_identifier` TEXT
- `sms_webhook_secret` TEXT
- `sms_source_active` BOOLEAN DEFAULT true
- `sms_last_seen_at` TIMESTAMPTZ
- `sms_message_count` INTEGER DEFAULT 0

### 3. ✅ Migrated Data
- SMS source data migrated from `institutions` to `transactions`
- Linked by `institution_id` to match transactions with their SMS source config

### 4. ✅ Created Indexes
- `idx_transactions_sms_device_identifier`
- `idx_transactions_sms_source_type`
- `idx_transactions_sms_source_active`

## Final Structure

### `institutions` Table
- ✅ Contains: `primary_momo_code`, `additional_momo_codes` (from institution_momo_codes)
- ❌ Does NOT contain: SMS source columns (removed - wrong place)

### `transactions` Table  
- ✅ Contains: All SMS source columns (from sms_sources)
- ✅ Contains: Raw SMS (`raw_sms_text`)
- ✅ Contains: Parsed transaction data
- ✅ Contains: Allocations
- ✅ Contains: Everything in one place!

## Verification

```sql
-- Verify SMS source columns are in transactions
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'transactions' 
AND column_name LIKE 'sms_%';
-- Should show: sms_source_name, sms_source_type, etc.

-- Verify SMS source columns are NOT in institutions
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'institutions' 
AND column_name LIKE 'sms_%';
-- Should return: 0 rows (all removed)

-- Verify tables are dropped
SELECT table_name 
FROM information_schema.tables 
WHERE table_name IN ('sms_sources', 'institution_momo_codes');
-- Should return: 0 rows (all dropped)
```

## Summary

✅ **institution_momo_codes** → Merged into `institutions` (CORRECT)
✅ **sms_sources** → Merged into `transactions` (FIXED - now correct)
✅ Both source tables dropped
✅ Data properly migrated

---

**Status**: ✅ **ALL MERGES CORRECTED - SMS SOURCES NOW IN TRANSACTIONS TABLE**
