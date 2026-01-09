# Quick Deployment Guide

## Recommended Approach: Dashboard + CLI Hybrid

### Part 1: Apply Migration via Supabase Dashboard (Easiest)

1. **Open Supabase Dashboard**:
   - Go to: https://supabase.com/dashboard/project/wadhydemushqqtcrrlwm

2. **Navigate to SQL Editor**:
   - Click **SQL Editor** in left sidebar
   - Click **New query**

3. **Copy and Run Migration**:
   - Open file: `supabase/migrations/20250129_create_transactions_table.sql`
   - Copy ALL contents
   - Paste into SQL Editor
   - Click **Run** (or press Cmd+Enter / Ctrl+Enter)

4. **Verify Success**:
   - Should see "Success. No rows returned"
   - Run this to verify table exists:
     ```sql
     SELECT COUNT(*) FROM transactions;
     ```
   - Should return `0` (empty table, but exists)

### Part 2: Deploy Function via CLI (If Linked) OR Dashboard

#### Option A: Via CLI (If project is linked)

```bash
cd /Users/jeanbosco/workspace/SMSGateway-Android

# Link project first (if not already linked)
supabase link --project-ref wadhydemushqqtcrrlwm
# Enter your database password when prompted

# Deploy function
supabase functions deploy ingest-sms
```

#### Option B: Via Dashboard (If CLI not linked)

1. Go to Supabase Dashboard → **Edge Functions**
2. Find **ingest-sms** function
3. Click **Edit** or **Deploy from local**
4. OR use **GitHub integration** if you have it set up

#### Option C: Manual Upload via Dashboard

1. Go to **Edge Functions** → **Create new function** (or edit existing)
2. Name: `ingest-sms`
3. Copy contents of: `supabase/functions/ingest-sms/index.ts`
4. Paste into function editor
5. Copy contents of: `supabase/functions/ingest-sms/deno.json`
6. Add to function settings/configuration
7. Click **Deploy**

## Alternative: Full CLI Approach

If you prefer everything via CLI:

```bash
cd /Users/jeanbosco/workspace/SMSGateway-Android

# 1. Login (if needed)
supabase login

# 2. Link project
supabase link --project-ref wadhydemushqqtcrrlwm

# 3. Push migration
supabase db push

# 4. Deploy function
supabase functions deploy ingest-sms
```

## Verification After Deployment

### Check Transactions Table

Run in Supabase SQL Editor:

```sql
-- Verify table structure
\d transactions

-- Or
SELECT 
  column_name, 
  data_type 
FROM information_schema.columns
WHERE table_name = 'transactions'
ORDER BY ordinal_position;
```

### Check Function is Updated

1. Go to **Edge Functions** → **ingest-sms**
2. View code - should contain transaction creation logic
3. Check **Logs** tab after sending test SMS

### Test End-to-End

1. Send test MTN MoMo payment SMS to device
2. Wait for processing (few seconds)
3. Check `sms_messages` table - should have parsed SMS
4. Check `transactions` table - should have new transaction record
5. Verify link: `transactions.sms_message_id = sms_messages.id`

## Troubleshooting

### "Project not found" when linking

- Double-check project ref: `wadhydemushqqtcrrlwm`
- Make sure you have access to the project
- Try: `supabase projects list` to see available projects

### Migration fails with foreign key error

- Make sure `sms_messages` table exists first
- Check if you ran previous migrations: `20250128_add_fingerprint_and_momo_code.sql`

### Function deployment fails

- Check you're logged in: `supabase login`
- Verify project is linked: Check for `.supabase` folder or run `supabase status`
- Check function syntax: The TypeScript/Deno code should be valid
