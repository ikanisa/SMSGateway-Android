# ✅ Institutions Table Merge Complete

## Summary

Successfully merged `sms_sources` and `institution_momo_codes` tables into the `institutions` table.

## What Was Done

### 1. ✅ Merged `institution_momo_codes` into `institutions`

**Columns Added:**
- `primary_momo_code` TEXT - Primary/default MoMo code
- `additional_momo_codes` JSONB - Array of all MoMo codes for the institution
  - Format: `[{"momo_code": "...", "is_active": true, "is_primary": false}]`

**Data Migration:**
- Primary MoMo code set from `institution_momo_codes` where `is_primary = true`
- All MoMo codes (primary + additional) stored in `additional_momo_codes` JSONB array
- Existing `momo_code` column updated to match `primary_momo_code`

### 2. ✅ Merged `sms_sources` into `institutions`

**Columns Added:**
- `sms_source_name` TEXT - Name of SMS source/device
- `sms_source_type` TEXT DEFAULT 'android_gateway' - Type of source
- `sms_device_identifier` TEXT - Device identifier for Android gateways
- `sms_webhook_secret` TEXT - Webhook secret for webhook sources
- `sms_source_active` BOOLEAN DEFAULT true - Whether source is active
- `sms_last_seen_at` TIMESTAMPTZ - Last time source sent SMS
- `sms_message_count` INTEGER DEFAULT 0 - Total messages from source

**Data Migration:**
- SMS source configuration migrated from `sms_sources` to matching institution by `institution_id`

### 3. ✅ Tables Dropped

| Table | Status | Reason |
|-------|--------|--------|
| `sms_sources` | ✅ **DROPPED** | Merged into `institutions` table |
| `institution_momo_codes` | ✅ **DROPPED** | Merged into `institutions` table |

### 4. ✅ Indexes Created

- `idx_institutions_sms_device_identifier` - For device lookup
- `idx_institutions_primary_momo_code` - For MoMo code lookup
- `idx_institutions_sms_source_active` - For active sources

## Final Institutions Table Structure

The `institutions` table now contains:

**Existing Columns:**
- `id`, `name`, `code`, `status`, `type`
- `momo_code` (updated to match primary_momo_code)
- `momo_ussd_code`
- `phone`, `email`, `address`, etc.

**New Columns (from institution_momo_codes):**
- `primary_momo_code` - Primary MoMo code
- `additional_momo_codes` - JSONB array of all MoMo codes

**New Columns (from sms_sources):**
- `sms_source_name` - Source/device name
- `sms_source_type` - Source type (android_gateway, webhook)
- `sms_device_identifier` - Device identifier
- `sms_webhook_secret` - Webhook secret
- `sms_source_active` - Active status
- `sms_last_seen_at` - Last activity
- `sms_message_count` - Message count

## Benefits

✅ **Single Source of Truth**: All institution-related data in one table
✅ **Simplified Schema**: No need to join multiple tables
✅ **Multiple MoMo Codes**: Supported via JSONB array
✅ **SMS Source Config**: Integrated into institution record
✅ **Cleaner Architecture**: Fewer tables to manage

## Usage Examples

### Get Institution with MoMo Codes:
```sql
SELECT 
  id,
  name,
  momo_code, -- Primary code
  primary_momo_code, -- Same as momo_code
  additional_momo_codes -- All codes as JSONB array
FROM institutions
WHERE id = '...';
```

### Get Institution with SMS Source:
```sql
SELECT 
  id,
  name,
  sms_source_name,
  sms_source_type,
  sms_device_identifier,
  sms_source_active
FROM institutions
WHERE sms_source_active = true;
```

### Query by MoMo Code:
```sql
-- Primary code
SELECT * FROM institutions WHERE momo_code = '*182*8*1*123456#';

-- Or in additional codes (JSONB query)
SELECT * FROM institutions 
WHERE additional_momo_codes @> '[{"momo_code": "*182*8*1*654321#"}]'::jsonb;
```

## Verification

```sql
-- Verify tables are dropped
SELECT table_name 
FROM information_schema.tables 
WHERE table_name IN ('sms_sources', 'institution_momo_codes');
-- Expected: 0 rows

-- Verify columns exist
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'institutions' 
AND (column_name LIKE '%sms%' OR column_name LIKE '%momo%')
ORDER BY column_name;
-- Should show all new columns
```

---

**Status**: ✅ **ALL TABLES MERGED INTO INSTITUTIONS - CLEANUP COMPLETE**
