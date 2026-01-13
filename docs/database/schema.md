# SMS Sources Table Analysis

## Table Structure

The `sms_sources` table is a **configuration/lookup table** that tracks different **sources** from which SMS messages can be received.

### Columns:

| Column | Type | Purpose |
|--------|------|---------|
| `id` | UUID | Primary key |
| `institution_id` | UUID | Links to institution (multi-tenancy) |
| `name` | TEXT | Human-readable name (e.g., "Office Phone Samsung A52") |
| `source_type` | TEXT | Type of source: `android_gateway`, `webhook`, etc. |
| `device_identifier` | TEXT | Device identifier (for Android gateway sources) |
| `webhook_secret` | TEXT | Secret key for webhook authentication |
| `is_active` | BOOLEAN | Whether this source is currently active |
| `last_seen_at` | TIMESTAMPTZ | Last time this source sent an SMS |
| `message_count` | INTEGER | Total count of messages from this source |
| `display_name` | TEXT | Optional display name |
| `created_at` | TIMESTAMPTZ | When source was registered |
| `updated_at` | TIMESTAMPTZ | Last update time |

## Actual Data Examples

From your database, the table contains:

1. **Android Gateway Sources:**
   - "Office Phone Samsung A52" (device: device-samsung-a52-001)
   - "Branch 1 Phone" (device: device-xiaomi-redmi-003)

2. **Webhook Sources:**
   - "Primary Webhook" (with webhook_secret)

## Purpose

This table is designed to:

1. **Track SMS Sources**: Identify WHERE SMS messages are coming from (Android devices, webhooks, APIs, etc.)

2. **Multi-Source Support**: Support multiple ways to receive SMS:
   - Android Gateway App (your current implementation)
   - Webhook endpoints (for receiving SMS from external services)
   - API integrations
   - Other future sources

3. **Institution Mapping**: Link sources to specific institutions (multi-tenancy)

4. **Source Configuration**: Store configuration per source:
   - Device identifiers for Android gateways
   - Webhook secrets for webhook authentication
   - Active/inactive status

5. **Analytics**: Track message counts and last seen timestamps per source

## Current Usage Status

### ❌ **NOT Currently Used in ingest-sms Function**

The `ingest-sms` function currently:
- Hardcodes `source: "android"` 
- Does NOT reference `sms_sources` table
- Does NOT use `sms_source_id` to link to this table

### Potential Integration

The table was likely designed to:
- Reference from `transactions.sms_source_id` (which exists but is NULL)
- Validate webhook requests using `webhook_secret`
- Track which Android device sent which SMS
- Provide analytics per source

## Recommendation

### Option 1: **KEEP** (Recommended if you plan to use multiple sources)
- Keep if you will use webhooks or multiple Android devices
- Keep if you need source tracking/analytics
- Keep if you need multi-institution source management

### Option 2: **REMOVE** (If not needed)
- Remove if you only use Android gateway
- Remove if you don't need source tracking
- Remove if it's unused legacy code

## Current Implementation Gap

The `transactions` table has a `source_sms_id` column that could reference `sms_sources.id`, but:
- It's not being populated by `ingest-sms`
- The function hardcodes `source: "android"` as a string
- No relationship exists currently

## Suggested Next Steps

If you want to **use** this table:

1. **Link Android devices to sms_sources**:
   ```sql
   UPDATE transactions 
   SET source_sms_id = (
     SELECT id FROM sms_sources 
     WHERE device_identifier = 'device-samsung-a52-001' 
     AND source_type = 'android_gateway'
   )
   WHERE source = 'android';
   ```

2. **Update ingest-sms function** to:
   - Lookup `sms_source_id` based on device identifier or momo_code
   - Store `source_sms_id` in transactions
   - Update `last_seen_at` and `message_count` in sms_sources

If you want to **remove** this table:
- It's safe to drop if not used elsewhere
- No foreign key constraints to transactions currently
