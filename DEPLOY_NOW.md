# ⚡ Deploy Now - Simple Steps

## ✅ Ready to Deploy

All code is ready:
- ✅ Migration file: `supabase/migrations/20250129_create_transactions_table.sql`
- ✅ Updated function: `supabase/functions/ingest-sms/index.ts`

## 🚀 Quick Deployment (Dashboard Method)

### Step 1: Create Transactions Table (2 minutes)

1. **Open**: https://supabase.com/dashboard/project/wadhydemushqqtcrrlwm/sql/new

2. **Copy** the entire file: `supabase/migrations/20250129_create_transactions_table.sql`

3. **Paste** into SQL Editor

4. **Run** (Cmd+Enter / Ctrl+Enter)

5. **Verify** - Run this:
   ```sql
   SELECT COUNT(*) FROM transactions;
   ```
   Should return `0` (empty table, but exists ✅)

### Step 2: Update Function (3 minutes)

1. **Open**: https://supabase.com/dashboard/project/wadhydemushqqtcrrlwm/functions

2. **Click** on `ingest-sms` function

3. **Click** Edit button

4. **Copy** entire file: `supabase/functions/ingest-sms/index.ts`

5. **Replace** all code in editor

6. **Click** Deploy/Save

7. **Verify** - Code should contain:
   - `Create transaction record if this is a valid MTN MoMo payment transaction`
   - `Transaction record created for SMS ${id}`

## ✅ Verification (1 minute)

After deployment, send a test MTN MoMo SMS and check:

```sql
-- Check if transaction was created
SELECT 
    t.id,
    t.txn_type,
    t.amount,
    t.sms_message_id,
    s.body
FROM transactions t
JOIN sms_messages s ON t.sms_message_id = s.id
ORDER BY t.created_at DESC
LIMIT 1;
```

Should show transaction linked to SMS! ✅

---

**That's it!** Everything is deployed and ready.

For detailed instructions, see: `DASHBOARD_DEPLOYMENT.md`
