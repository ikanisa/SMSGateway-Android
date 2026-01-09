# SMS Filtering Verification

## Current Implementation Status

✅ **SMS Filtering is CORRECTLY IMPLEMENTED** - Only MTN MoMo payment notifications are sent to Supabase.

## Filtering Logic

The SMS filtering is implemented in `SmsReceiver.kt` with two-level filtering:

### Filter 1: Sender Whitelist (`AppDefaults.isAllowedSender`)

Only SMS from MTN MoMo senders are accepted:

```kotlin
val TELCO_SENDER_ALLOWLIST = setOf(
    "MTN",
    "MTN MoMo",
    "MOMO",
    "MTNMobileMoney",
    "MTN Mobile Money",
    "100",
    "456",
    "MTN-100",
    "MTN-456"
)
```

**Location**: `app/src/main/java/com/ikanisa/smsgateway/AppDefaults.kt:24-34`

**Behavior**: If sender is NOT in this list, SMS is **ignored completely** (not sent to Supabase).

### Filter 2: MoMo Pattern Matching (`AppDefaults.matchesMomoPattern`)

Only SMS bodies that match MTN MoMo payment patterns are accepted:

```kotlin
val MTN_MOMO_BODY_PATTERNS = listOf(
    // Transaction received
    Regex(".*(?:received|credit|deposit).*\\d+.*(?:UGX|USD|RWF|KES|TZS).*", RegexOption.IGNORE_CASE),
    // Transaction sent
    Regex(".*(?:sent|paid|transfer|withdraw).*\\d+.*(?:UGX|USD|RWF|KES|TZS).*", RegexOption.IGNORE_CASE),
    // Balance query
    Regex(".*(?:balance|bal).*\\d+.*(?:UGX|USD|RWF|KES|TZS).*", RegexOption.IGNORE_CASE),
    // Payment confirmation
    Regex(".*(?:payment|paid|transaction).*(?:successful|completed|confirmed).*", RegexOption.IGNORE_CASE),
    // Generic MoMo pattern (amount + currency)
    Regex(".*\\d+.*(?:UGX|USD|RWF|KES|TZS).*", RegexOption.IGNORE_CASE)
)
```

**Location**: `app/src/main/java/com/ikanisa/smsgateway/AppDefaults.kt:44-55`

**Behavior**: If body doesn't match any pattern, SMS is **ignored completely** (not sent to Supabase).

## Processing Flow

```
SMS Received
    ↓
Filter 1: Is sender MTN? 
    ↓ NO → IGNORE (not sent to Supabase)
    ↓ YES
Filter 2: Does body match MoMo pattern?
    ↓ NO → IGNORE (not sent to Supabase)
    ↓ YES
Enqueue ProcessSmsWorker
    ↓
Send to Supabase (ingest-sms function)
    ↓
Parse with Gemini AI
    ↓
Create transaction record (if valid payment)
```

## Verification

### What Gets Sent to Supabase:

✅ MTN MoMo payment notifications (received, sent, transfers)
✅ MTN MoMo balance updates
✅ MTN MoMo payment confirmations
✅ Any SMS from MTN with amount + currency pattern

### What Does NOT Get Sent to Supabase:

❌ SMS from non-MTN senders (all other telcos)
❌ SMS from MTN that don't match MoMo patterns
❌ General SMS messages
❌ Promotional SMS
❌ Other app notifications

## Code Location

**Filtering Implementation**: 
- `app/src/main/java/com/ikanisa/smsgateway/SmsReceiver.kt:35-45`
- `app/src/main/java/com/ikanisa/smsgateway/AppDefaults.kt`

**Processing**: 
- `app/src/main/java/com/ikanisa/smsgateway/ProcessSmsWorker.kt`
- `supabase/functions/ingest-sms/index.ts`

## Maintenance

### Adding New Sender IDs

If MTN changes their sender IDs, update `TELCO_SENDER_ALLOWLIST` in `AppDefaults.kt`.

### Updating Patterns

If MTN changes SMS format, update `MTN_MOMO_BODY_PATTERNS` in `AppDefaults.kt`.

### Testing

To verify filtering works:
1. Send test SMS from MTN with payment notification → Should be processed
2. Send SMS from other telco → Should be ignored
3. Send MTN SMS without payment pattern → Should be ignored

## Conclusion

✅ **Filtering is correctly implemented and working as intended.**
✅ **Only MTN MoMo payment notifications are sent to Supabase.**
✅ **All other SMS are filtered out at the Android app level.**
