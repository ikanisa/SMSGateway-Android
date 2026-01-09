# ✅ Database Consolidation Complete

## Summary

All SMS/MoMo related tables have been successfully consolidated into a single **`transactions`** table as requested.

## What Was Done

### 1. ✅ Tables Consolidated
- **momo_sms_raw** → Merged into `transactions.raw_sms_text`
- **sms_parse_attempts** → Merged into `transactions.parse_attempts` and `parse_error`
- **transaction_allocations** → Already in `transactions` (allocation_status, allocated_by, etc.)
- **momo_parsed_transactions** → Merged into `transactions` (parsed transaction data)

### 2. ✅ Views Dropped
- **vw_transactions_enriched** → Dropped (no longer needed)
- **vw_transactions_consolidated** → Dropped (no longer needed)

### 3. ✅ Tables Dropped
- ✅ `momo_sms_raw` - Dropped (data consolidated)
- ✅ `momo_parsed_transactions` - Dropped (data consolidated)
- ✅ `transaction_allocations` - Dropped (already in transactions)
- ✅ `sms_parse_attempts` - Dropped (data consolidated)

### 4. ✅ Columns Added to Transactions Table

**Raw SMS:**
- `raw_sms_text` TEXT - **ONE column for raw MoMo SMS** (as requested)

**SMS Metadata:**
- `sms_hash` TEXT (unique) - For deduplication
- `message_hash` TEXT
- `received_at` TIMESTAMPTZ
- `momo_code` TEXT
- `device_id` UUID

**Parsing:**
- `parse_status` TEXT DEFAULT 'pending'
- `parse_error` TEXT
- `parse_attempts` INTEGER DEFAULT 0
- `parse_version` TEXT
- `parse_confidence` NUMERIC

**Resolution:**
- `resolution_status` TEXT
- `resolution_note` TEXT
- `resolved_by` UUID
- `resolved_at` TIMESTAMPTZ

**Metadata:**
- `meta` JSONB - Flexible metadata storage
- `ingested_at` TIMESTAMPTZ

### 5. ✅ Existing Columns Used
- `payer_phone` - Maps to sender_phone from raw SMS
- `occurred_at` - Maps to received_at from raw SMS
- `momo_sms_id` - References original SMS (self-reference)
- All existing transaction fields (type, amount, currency, etc.)

### 6. ✅ Constraints Updated
- Made `type`, `amount`, `currency`, `channel` nullable so raw SMS can be inserted without parsed data
- Added unique constraint on `sms_hash` for deduplication
- Added comprehensive indexes for performance

### 7. ✅ Function Updated
- **ingest-sms** function updated to insert directly into `transactions` table
- Inserts raw SMS into `raw_sms_text` column
- Parses SMS and updates same transaction record with parsed data
- No separate table inserts needed

## New Workflow

### SMS Gateway Flow:
1. **SMS Received** → Android app sends to `ingest-sms` function
2. **Insert Raw SMS** → Direct insert into `transactions` table with:
   - `raw_sms_text` = SMS body (ONE column as requested)
   - `sms_hash` = SHA256 fingerprint for deduplication
   - `parse_status` = 'pending'
   - Other metadata fields
3. **Parse SMS** → Gemini AI extracts transaction details
4. **Update Transaction** → Same transaction record updated with:
   - `type`, `amount`, `currency` (parsed transaction fields)
   - `parse_status` = 'parsed'
   - Parsed metadata in `meta` JSONB column
5. **Done** → Single transaction record contains everything

## Benefits

✅ **Single Source of Truth**: All SMS and transaction data in one table
✅ **Simplified Architecture**: No joins needed between multiple tables
✅ **One Column for Raw SMS**: `raw_sms_text` as requested
✅ **Direct Insert**: SMS gateway feeds directly into transactions table
✅ **All Processing in Functions**: Parsing logic handles everything after insert
✅ **Backward Compatible**: Existing transaction fields still work
✅ **Deduplication**: Unique constraint on `sms_hash` prevents duplicates

## Verification

```sql
-- Check raw_sms_text column exists
SELECT EXISTS (
  SELECT FROM information_schema.columns 
  WHERE table_name = 'transactions' 
  AND column_name = 'raw_sms_text'
);
-- Should return: true

-- Check old tables are dropped
SELECT EXISTS (
  SELECT FROM information_schema.tables 
  WHERE table_name = 'momo_sms_raw'
);
-- Should return: false

-- Check unique constraint on sms_hash
SELECT conname 
FROM pg_constraint 
WHERE conname = 'transactions_sms_hash_unique';
-- Should return: transactions_sms_hash_unique
```

## Function Deployment

✅ **ingest-sms** function deployed and active
- Inserts directly into `transactions` table
- Uses `raw_sms_text` for raw SMS
- Updates same record with parsed data
- Handles all parsing logic

## Migration File

✅ Migration saved to: `supabase/migrations/20250129_consolidate_transactions.sql`

---

**Status**: ✅ **CONSOLIDATION COMPLETE - ALL TABLES MERGED INTO TRANSACTIONS**
