# Dashboard Deployment Guide - No CLI Required

Since CLI linking requires additional permissions, use the Supabase Dashboard for both steps.

## Step 1: Apply Transactions Table Migration

### Via SQL Editor

1. **Open Supabase Dashboard**:
   - Go to: https://supabase.com/dashboard/project/wadhydemushqqtcrrlwm
   - Or navigate: Dashboard → Your Project → SQL Editor

2. **Create New Query**:
   - Click **SQL Editor** in left sidebar
   - Click **New query** button

3. **Run Migration**:
   - Copy the ENTIRE contents of: `supabase/migrations/20250129_create_transactions_table.sql`
   - Paste into the SQL Editor
   - Click **Run** button (or press `Cmd+Enter` / `Ctrl+Enter`)

4. **Verify Success**:
   - Should see green checkmark: "Success. No rows returned"
   - Verify table exists:
     ```sql
     SELECT table_name 
     FROM information_schema.tables 
     WHERE table_schema = 'public' 
     AND table_name = 'transactions';
     ```
   - Should return 1 row with `transactions`

## Step 2: Update Edge Function

### Option A: Edit Function in Dashboard

1. **Navigate to Edge Functions**:
   - Click **Edge Functions** in left sidebar
   - Find **ingest-sms** function
   - Click on it to open

2. **Update Function Code**:
   - Click **Edit** or pencil icon
   - Open file: `supabase/functions/ingest-sms/index.ts`
   - Copy ALL contents
   - Replace the entire function code in Dashboard
   - Click **Deploy** or **Save**

3. **Update Function Configuration** (if needed):
   - Check **Settings** tab
   - Ensure Deno version is set correctly
   - Verify environment variables are set (if any)

### Option B: Create New Function Version

If you can't edit existing function:

1. **Create New Version**:
   - Go to **Edge Functions** → **Create new function**
   - Name: `ingest-sms` (will overwrite or create new)
   - Copy contents from `supabase/functions/ingest-sms/index.ts`

2. **Set Function Configuration**:
   - Runtime: Deno
   - Entrypoint: `index.ts`
   - Copy contents from `supabase/functions/ingest-sms/deno.json` for imports

3. **Deploy**:
   - Click **Deploy** button

## Step 3: Verify Deployment

### Verify Transactions Table

Run in SQL Editor:

```sql
-- Check table structure
SELECT 
    column_name, 
    data_type, 
    is_nullable
FROM information_schema.columns
WHERE table_name = 'transactions'
ORDER BY ordinal_position;

-- Should show all columns including:
-- id, sms_message_id, txn_id, ft_id, txn_type, amount, currency, etc.
```

### Verify Function is Updated

1. **Check Function Code**:
   - Go to **Edge Functions** → **ingest-sms**
   - View code - should contain transaction creation logic
   - Look for: `Create transaction record if this is a valid MTN MoMo payment transaction`

2. **Test with Logs**:
   - Send test MTN MoMo payment SMS
   - Go to **Edge Functions** → **ingest-sms** → **Logs** tab
   - Look for: `Transaction record created for SMS {id}`

### Test End-to-End

1. **Send Test SMS**:
   - Send MTN MoMo payment notification SMS to device
   - Wait 5-10 seconds for processing

2. **Check SMS Message**:
   ```sql
   SELECT id, sender, body, parse_status, created_at
   FROM sms_messages
   ORDER BY created_at DESC
   LIMIT 1;
   ```
   - Should show parsed SMS with `parse_status = 'parsed'`

3. **Check Transaction**:
   ```sql
   SELECT 
       t.id,
       t.txn_type,
       t.amount,
       t.currency,
       t.status,
       t.sms_message_id,
       s.body as sms_body
   FROM transactions t
   JOIN sms_messages s ON t.sms_message_id = s.id
   ORDER BY t.created_at DESC
   LIMIT 1;
   ```
   - Should show transaction record linked to SMS
   - `sms_message_id` should match SMS `id`

## Quick SQL Commands for Verification

### Check Migration Applied

```sql
-- Verify transactions table exists
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = 'transactions'
);

-- Should return: true
```

### Check Recent Activity

```sql
-- View recent transactions with SMS details
SELECT 
    t.id as transaction_id,
    t.txn_type,
    t.amount,
    t.currency,
    t.status,
    t.created_at as transaction_created,
    s.id as sms_id,
    s.parse_status,
    s.created_at as sms_created
FROM transactions t
JOIN sms_messages s ON t.sms_message_id = s.id
ORDER BY t.created_at DESC
LIMIT 10;
```

### Check Function Logs (via Dashboard)

1. Go to **Edge Functions** → **ingest-sms** → **Logs**
2. Filter by time range (last hour/day)
3. Look for:
   - ✅ `Transaction record created for SMS {id}` - Success
   - ❌ `Failed to create transaction record` - Error (check details)

## Troubleshooting

### Migration Fails

**Error: "relation sms_messages does not exist"**
- Solution: Run previous migrations first:
  - `20250128_add_fingerprint_and_momo_code.sql`
  - Check if `sms_messages` table exists

**Error: "permission denied"**
- Solution: Make sure you're using the correct database user
- Use service role key if needed (for admin operations)

### Function Not Creating Transactions

1. **Check Function Logs**:
   - Go to Edge Functions → ingest-sms → Logs
   - Look for error messages

2. **Verify SMS Parsing**:
   ```sql
   SELECT id, parse_status, parse_error 
   FROM sms_messages 
   WHERE created_at > NOW() - INTERVAL '1 hour'
   ORDER BY created_at DESC;
   ```
   - If `parse_status = 'failed'`, check `parse_error` column

3. **Check Transaction Validation**:
   - Transaction must have: `amount > 0` and valid `txn_type`
   - Provider must be MTN MoMo
   - Check parsed data in `sms_messages.parsed` JSONB column

## Success Criteria

✅ Transactions table created with all columns  
✅ Function code updated with transaction creation logic  
✅ Test SMS creates transaction record  
✅ Transaction linked to SMS via `sms_message_id`  
✅ Function logs show successful transaction creation  

---

**Note**: All files are ready:
- Migration: `supabase/migrations/20250129_create_transactions_table.sql`
- Updated Function: `supabase/functions/ingest-sms/index.ts`
