# Final Deployment Instructions

## ✅ Status: Function Deployed Successfully

The `ingest-sms` function has been **successfully deployed** with transaction creation logic.

```
✅ Deployed Functions on project wadhydemushqqtcrrlwm: ingest-sms
✅ Dashboard: https://supabase.com/dashboard/project/wadhydemushqqtcrrlwm/functions
```

## ⚠️ Remaining Step: Apply Migration

Due to migration history sync issues, the transactions table migration must be applied manually via **Supabase Dashboard SQL Editor** (takes 2 minutes).

## Quick Migration Application (2 minutes)

### Step 1: Open SQL Editor

**Direct Link**: https://supabase.com/dashboard/project/wadhydemushqqtcrrlwm/sql/new

Or navigate:
1. Go to: https://supabase.com/dashboard/project/wadhydemushqqtcrrlwm
2. Click **SQL Editor** in left sidebar
3. Click **New query**

### Step 2: Copy Migration SQL

**File to copy**: `supabase/migrations/20250129_create_transactions_table.sql`

**OR use ready file**: `apply_transactions_migration.sql`

Copy the **entire contents** of the file.

### Step 3: Paste and Run

1. Paste into SQL Editor
2. Click **Run** button (or press `Cmd+Enter` / `Ctrl+Enter`)
3. Wait for execution (should take 1-2 seconds)

### Step 4: Verify Success

You should see: **"Success. No rows returned"** ✅

Verify table exists:
```sql
SELECT COUNT(*) FROM transactions;
```
Should return: `0` (empty table, but exists)

## Alternative: Using psql (If you have database password)

If you have the database password, you can use psql directly:

```bash
# Get password from: Supabase Dashboard → Settings → Database → Database password

psql "postgresql://postgres:[YOUR_PASSWORD]@db.wadhydemushqqtcrrlwm.supabase.co:5432/postgres" \
  < supabase/migrations/20250129_create_transactions_table.sql
```

## Verification After Migration

### 1. Check Table Structure

```sql
SELECT 
    column_name, 
    data_type, 
    is_nullable
FROM information_schema.columns
WHERE table_name = 'transactions'
ORDER BY ordinal_position;
```

Should show all transaction columns: id, sms_message_id, txn_type, amount, currency, etc.

### 2. Test End-to-End

1. **Send Test SMS**: Send MTN MoMo payment notification to device
2. **Check SMS Parsed**:
   ```sql
   SELECT id, sender, parse_status, created_at
   FROM sms_messages
   ORDER BY created_at DESC
   LIMIT 1;
   ```
3. **Check Transaction Created**:
   ```sql
   SELECT 
       t.id,
       t.txn_type,
       t.amount,
       t.currency,
       t.sms_message_id,
       s.body as sms_body
   FROM transactions t
   JOIN sms_messages s ON t.sms_message_id = s.id
   ORDER BY t.created_at DESC
   LIMIT 1;
   ```

Should show transaction linked to SMS! ✅

### 3. Check Function Logs

Go to: https://supabase.com/dashboard/project/wadhydemushqqtcrrlwm/functions/ingest-sms/logs

Look for:
- ✅ `Transaction record created for SMS {id}` - Success
- ❌ `Failed to create transaction record` - Error (check details)

## Summary

| Component | Status | Action |
|-----------|--------|--------|
| ✅ Function: ingest-sms | **DEPLOYED** | Complete - No action needed |
| ⚠️ Migration: transactions | **PENDING** | **Apply via Dashboard SQL Editor** (2 min) |
| ✅ SMS Filtering | Working | Only MTN MoMo SMS processed |
| ✅ Firebase | Configured | Already set up |

## Files Ready

- ✅ Migration SQL: `supabase/migrations/20250129_create_transactions_table.sql`
- ✅ Ready-to-use: `apply_transactions_migration.sql`
- ✅ Function: Deployed and active

---

**Next Action**: Apply migration via Dashboard SQL Editor (link above) to complete deployment! 🚀
