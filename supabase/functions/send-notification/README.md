# Send Notification Edge Function

This Supabase Edge Function handles sending SMS notifications to payers via various SMS gateway providers.

## Configuration

Set environment variables in your Supabase project to configure the SMS provider:

### Option 1: MTN SMS V3 API (Default & Recommended)

The MTN SMS V3 API is the primary SMS provider, supporting multiple African markets including Rwanda, Nigeria, Ghana, South Africa, and more.

**Prerequisites:**
1. Register for an MTN Developer Portal account at https://developer.mtn.com
2. Create an application to get your Consumer Key and Consumer Secret
3. Request Sender ID whitelisting for your target countries (required for production)

```bash
SMS_PROVIDER=MTN
MTN_CONSUMER_KEY=your_consumer_key
MTN_CONSUMER_SECRET=your_consumer_secret
MTN_SENDER_ADDRESS=MTN  # Your approved Sender ID (must be whitelisted)
MTN_SENDER_NAME=MTN  # Optional, defaults to MTN
MTN_TARGET_ENVIRONMENT=sandbox  # or "production" for live environment
MTN_NOTIFY_URL=https://your-callback-url.com/dlr  # Optional: for delivery receipts
MTN_CALLBACK_DATA=optional-callback-data  # Optional: custom data for webhook
```

**Important Notes:**
- Sender ID (`MTN_SENDER_ADDRESS`) must be pre-registered and whitelisted by MTN for your target countries
- Use `sandbox` environment for testing, `production` for live deployments
- OAuth 2.0 access tokens are automatically cached and refreshed
- Supported countries: Benin, Cameroon, Congo, Côte d'Ivoire, Eswatini, Ghana, Guinea, Guinea-Bissau, Liberia, Nigeria, Rwanda, South Africa, Zambia

### Option 2: AWS SNS

```bash
SMS_PROVIDER=AWS_SNS
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=us-east-1
```

**Note**: AWS SNS requires AWS Signature Version 4. For production, use the AWS SDK or implement proper request signing.

### Option 3: MessageBird

```bash
SMS_PROVIDER=MESSAGEBIRD
MESSAGEBIRD_API_KEY=your_api_key
MESSAGEBIRD_ORIGINATOR=SMSGateway  # Optional, defaults to SMSGateway
```

### Option 4: Generic HTTP API

```bash
SMS_PROVIDER=GENERIC
SMS_GATEWAY_URL=https://your-sms-gateway.com/api/send
SMS_GATEWAY_API_KEY=your_api_key  # Optional
SMS_GATEWAY_METHOD=POST  # GET or POST, defaults to POST
```

## Setting Environment Variables in Supabase

1. Go to your Supabase project dashboard
2. Navigate to **Edge Functions** → **send-notification**
3. Click on **Settings** or **Configuration**
4. Add the environment variables above
5. Save and redeploy the function

## API Usage

### Request

```bash
POST https://your-project.supabase.co/functions/v1/send-notification
Headers:
  Authorization: Bearer YOUR_ANON_KEY
  Content-Type: application/json

Body:
{
  "type": "DAILY_REMINDER",
  "message": "Please continue your contributions",
  "sendToAll": true,
  "parameters": {
    "memberName": "John",
    "groupName": "Burimunsi Production"
  }
}
```

### Response

```json
{
  "success": true,
  "message": "Notifications processed: 10 sent, 0 failed",
  "sentCount": 10,
  "failedCount": 0,
  "errors": []
}
```

## Deployment

```bash
# Install Supabase CLI if not already installed
npm install -g supabase

# Login to Supabase
supabase login

# Link to your project
supabase link --project-ref your-project-ref

# Deploy the function
supabase functions deploy send-notification
```

## Testing

Test the function locally:

```bash
# Start Supabase locally
supabase start

# Deploy function locally
supabase functions serve send-notification

# Test with curl
curl -X POST http://localhost:54321/functions/v1/send-notification \
  -H "Authorization: Bearer YOUR_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "TEST",
    "message": "Test message",
    "recipientPhoneNumbers": ["+1234567890"]
  }'
```

## Troubleshooting

### SMS Not Sending

1. Check environment variables are set correctly
2. Verify phone numbers are in E.164 format (+country code)
3. Check Supabase function logs for errors
4. Verify SMS provider credentials are valid

**For MTN SMS API specifically:**
- Ensure Consumer Key and Consumer Secret are correct
- Verify Sender ID is whitelisted for the target country
- Check that you're using the correct environment (sandbox vs production)
- Verify OAuth token is being generated (check logs for "MTN access token obtained")
- Ensure the target country is supported by MTN SMS V3 API

### Phone Number Format

Phone numbers should be in E.164 format (tel: prefix is automatically added):
- ✅ `+250788123456` (Rwanda) - will be formatted to `tel:+250788123456`
- ✅ `tel:+250788123456` (already formatted)
- ✅ `+2348030000000` (Nigeria)
- ✅ `+1234567890` (US)
- ❌ `0788123456` (missing country code)
- ❌ `250788123456` (missing + sign)

The function will attempt to auto-format phone numbers to MTN's required `tel:+...` format.

### MTN API Specific Issues

**OAuth Token Errors:**
- Verify Consumer Key and Secret are correct
- Check that the scope `SEND-SMS` is included in your MTN application settings
- Tokens are cached for efficiency - if you change credentials, the cache may need to clear

**Sender ID Issues:**
- Sender IDs must be pre-approved by MTN for each country
- Contact MTN Developer Support to whitelist your Sender ID
- Sandbox environment may have different Sender ID requirements than production

**Rate Limits:**
- MTN API typically allows ~100 requests per minute (varies by account tier)
- If you hit rate limits, implement request queuing or retry logic
