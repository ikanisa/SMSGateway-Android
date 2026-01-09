# Deployment Steps - Transactions Table & Updated Function

## Option 1: Using Supabase Dashboard (Recommended for Migration)

### Step 1: Apply Migration via Dashboard

1. Go to your Supabase project: https://supabase.com/dashboard/project/wadhydemushqqtcrrlwm
2. Navigate to **SQL Editor**
3. Create a new query
4. Copy the entire contents of `supabase/migrations/20250129_create_transactions_table.sql`
5. Paste into the SQL Editor
6. Click **Run** or press `Ctrl+Enter` (Windows/Linux) or `Cmd+Enter` (Mac)
7. Verify success - you should see "Success. No rows returned"

### Step 2: Verify Migration

Run this query to verify the transactions table was created:

```sql
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name = 'transactions';
```

You should see `transactions` in the results.

## Option 2: Using Supabase CLI (For Function Deployment)

### Step 1: Link Project (if not already linked)

```bash
cd /Users/jeanbosco/workspace/SMSGateway-Android
supabase link --project-ref wadhydemushqqtcrrlwm
```

You'll be prompted to enter your database password. Get it from:
- Supabase Dashboard → Project Settings → Database → Database password

### Step 2: Push Migration via CLI

```bash
supabase db push
```

This will apply any new migrations, including the transactions table.

### Step 3: Deploy Updated Function

```bash
supabase functions deploy ingest-sms
```

## Option 3: Hybrid Approach (Migration via Dashboard + Function via CLI)

1. **Apply migration via Dashboard** (Option 1, Step 1)
2. **Deploy function via CLI** (Option 2, Step 3)

This is often the easiest approach.

## Verification Steps

After deployment, verify everything works:

### 1. Check Transactions Table Exists

```sql
-- Run in Supabase SQL Editor
SELECT 
  column_name, 
  data_type, 
  is_nullable
FROM information_schema.columns
WHERE table_name = 'transactions'
ORDER BY ordinal_position;
```

### 2. Test Transaction Creation

Send a test MTN MoMo payment SMS and verify:

1. SMS is parsed (check `sms_messages` table)
2. Transaction record is created (check `transactions` table)
3. Link between them exists (check `sms_message_id` in transactions)

```sql
-- Check recent transactions
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
LIMIT 10;
```

### 3. Check Function Logs

In Supabase Dashboard:
- Go to **Edge Functions** → **ingest-sms**
- Click on **Logs** tab
- Look for:
  - "Transaction record created for SMS {id}" messages
  - Any error messages

## Troubleshooting

### Migration Fails

If migration fails, check:
- Do you have the `sms_messages` table? (Required for foreign key)
- Are you running as a user with CREATE TABLE permissions?
- Check error message in SQL Editor

### Function Deployment Fails

If function deployment fails:
- Make sure you're logged in: `supabase login`
- Make sure project is linked: `supabase link --project-ref wadhydemushqqtcrrlwm`
- Check your access token is valid

### Transactions Not Being Created

If transactions aren't being created after deployment:
1. Check function logs for errors
2. Verify SMS is being parsed successfully (check `parse_status` in `sms_messages`)
3. Verify transaction validation logic (must have amount > 0 and valid txn_type)
4. Check provider is MTN MoMo

## Quick Deploy Script

You can create a script to automate this:

```bash
#!/bin/bash
# deploy.sh

echo "Deploying transactions migration and updated function..."

# Option 1: If using CLI for migration
# supabase db push

# Option 2: If using Dashboard for migration, just deploy function
supabase functions deploy ingest-sms

echo "Deployment complete!"
```

Save as `deploy.sh`, make executable: `chmod +x deploy.sh`, then run: `./deploy.sh`
