# Transactions Integration - MoMo Payments to Transactions Table

## Overview

MTN MoMo payment notifications parsed from SMS are now automatically linked to a `transactions` table in Supabase. This creates a proper transaction record for each payment while maintaining a link to the source SMS message.

## Flow

```
MTN MoMo SMS Received
    ↓
Filtered by SmsReceiver (only MTN MoMo payment SMS)
    ↓
Sent to Supabase ingest-sms function
    ↓
Parsed with Gemini AI
    ↓
Stored in sms_messages table
    ↓
IF valid payment transaction:
    ↓
Transaction record created in transactions table
    ↓
Linked via sms_message_id foreign key
```

## Database Schema

### transactions Table

Created by migration: `supabase/migrations/20250129_create_transactions_table.sql`

**Key Fields**:
- `sms_message_id` - Foreign key to `sms_messages.id` (links to source SMS)
- `txn_id` - Transaction ID from SMS (if available)
- `ft_id` - FT (Fund Transfer) ID from SMS (if available)
- `txn_type` - Transaction type: 'credit/received', 'debit/sent', 'cashout', 'payment', etc.
- `amount` - Transaction amount (required)
- `currency` - Currency code (default: 'RWF')
- `balance` - Balance after transaction
- `counterparty` - Counterparty name/number
- `provider` - Payment provider (default: 'MTN MoMo')
- `status` - Transaction status (default: 'confirmed')
- `momo_code` - Device/account identifier

## Transaction Creation Logic

Transactions are created **automatically** when:

1. ✅ SMS is successfully parsed (parse_status = 'parsed')
2. ✅ Transaction has a valid amount (> 0)
3. ✅ Transaction type is one of: credit, received, debit, sent, cashout, payment, transfer

**Location**: `supabase/functions/ingest-sms/index.ts` (after SMS parsing)

## Example Transaction Record

```json
{
  "id": "uuid-here",
  "sms_message_id": "sms-uuid-here",
  "txn_id": "TXN123456",
  "ft_id": "FT2422...",
  "txn_type": "credit/received",
  "amount": 5000.00,
  "currency": "RWF",
  "fee": 0.00,
  "balance": 25000.00,
  "counterparty": "John Doe",
  "counterparty_phone_suffix": "235",
  "reference": "Payment for services",
  "provider": "MTN MoMo",
  "status": "confirmed",
  "momo_code": "device-code-123",
  "transaction_time": "2025-01-29T10:30:00+02:00",
  "created_at": "2025-01-29T10:30:05+00:00"
}
```

## Querying Transactions

### Get all transactions for a device

```sql
SELECT * FROM transactions 
WHERE momo_code = 'device-code-123' 
ORDER BY transaction_time DESC;
```

### Get transaction with source SMS

```sql
SELECT 
  t.*,
  s.body as sms_body,
  s.sender as sms_sender
FROM transactions t
JOIN sms_messages s ON t.sms_message_id = s.id
WHERE t.momo_code = 'device-code-123';
```

### Get transactions by type

```sql
SELECT * FROM transactions 
WHERE txn_type IN ('credit', 'received')
AND amount > 0
ORDER BY transaction_time DESC;
```

### Get transactions in date range

```sql
SELECT * FROM transactions 
WHERE transaction_time >= '2025-01-01'
AND transaction_time < '2025-02-01'
ORDER BY transaction_time DESC;
```

## Integration with Notification System

Transactions can trigger notifications:

1. **Balance Update Notification**: After transaction is created, notify payer of updated balance
2. **Payment Received Notification**: When txn_type is 'credit/received'
3. **Payment Sent Notification**: When txn_type is 'debit/sent'

**Future Enhancement**: Add trigger or edge function to automatically send notifications when transactions are created.

## Idempotency

- ✅ Each SMS can only create **one transaction** (unique constraint on `sms_message_id`)
- ✅ If transaction already exists, it's skipped (prevents duplicates)
- ✅ SMS fingerprint prevents duplicate SMS from being processed

## Error Handling

- If transaction creation fails, SMS ingestion **still succeeds**
- Transaction errors are logged but don't block SMS processing
- Failed transaction creation can be retried by re-processing the SMS

## Maintenance

### Updating Transaction Status

```sql
UPDATE transactions 
SET status = 'confirmed', updated_at = NOW()
WHERE id = 'transaction-uuid';
```

### Linking to Payers

If you want to link transactions to payers table:

```sql
ALTER TABLE transactions 
ADD COLUMN payer_id UUID REFERENCES payers(id);

-- Link by phone number
UPDATE transactions t
SET payer_id = p.id
FROM payers p
WHERE t.counterparty_phone_suffix IS NOT NULL
AND p.phone_number LIKE '%' || t.counterparty_phone_suffix;
```

## Testing

1. Send MTN MoMo payment SMS
2. Check `sms_messages` table for parsed SMS
3. Check `transactions` table for created transaction
4. Verify `sms_message_id` links correctly

## Future Enhancements

- [ ] Automatic payer linking by phone number
- [ ] Transaction status updates from delivery receipts
- [ ] Transaction reconciliation
- [ ] Transaction analytics and reporting
- [ ] Webhook triggers for transaction events
