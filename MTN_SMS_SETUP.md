# MTN SMS V3 API Setup Guide

This guide will help you configure MTN SMS V3 API for sending SMS notifications in the SMS Gateway application.

## Overview

The MTN SMS V3 API is a RESTful service that enables sending SMS messages, receiving mobile-originated (MO) messages, and tracking delivery status. It's part of the MTN MADAPI (Mobile Application Development API) suite and serves multiple African markets including:

- **Rwanda** 🇷🇼
- **Nigeria** 🇳🇬
- **Ghana** 🇬🇭
- **South Africa** 🇿🇦
- **Benin, Cameroon, Congo, Côte d'Ivoire, Eswatini, Guinea, Guinea-Bissau, Liberia, Zambia**

## Prerequisites

1. MTN Developer Portal account
2. Application created in MTN Developer Portal
3. Consumer Key and Consumer Secret
4. Approved Sender ID (Alphanumeric Sender ID or Shortcode)

## Step 1: Register on MTN Developer Portal

1. Go to [MTN Developer Portal](https://developer.mtn.com)
2. Sign up for a new account or log in
3. Complete your profile and verification

## Step 2: Create Application

1. Navigate to **My Apps** in the developer portal
2. Click **Create New App**
3. Fill in application details:
   - **App Name**: SMS Gateway (or your preferred name)
   - **Description**: SMS notification system for Burimunsi Production
   - **Category**: Select appropriate category
4. Save and note your **Consumer Key** and **Consumer Secret**
   - ⚠️ **Important**: Store these securely - you'll need them for configuration

## Step 3: Request Sender ID Whitelisting

Sender IDs must be pre-approved and whitelisted by MTN before they can be used in production.

### For Rwanda (Example)

1. Contact MTN Rwanda support or use the developer portal
2. Request whitelisting for your desired Sender ID (e.g., "BURIMUNSI", "SMSGateway")
3. Provide business justification and use case
4. Wait for approval (can take a few days to weeks depending on country)

### Sender ID Requirements

- **Alphanumeric**: Up to 11 characters (e.g., "BURIMUNSI")
- **Shortcode**: Numeric codes (varies by country)
- Must comply with country-specific regulations
- Nigeria and Ghana have strict regulations - approval is mandatory

### Sandbox vs Production

- **Sandbox**: Testing environment, may have relaxed Sender ID requirements
- **Production**: Requires full approval and whitelisting

## Step 4: Configure Environment Variables

Set these environment variables in your Supabase Edge Function configuration:

### Required Variables

```bash
SMS_PROVIDER=MTN
MTN_CONSUMER_KEY=your_consumer_key_here
MTN_CONSUMER_SECRET=your_consumer_secret_here
MTN_SENDER_ADDRESS=MTN  # Your approved Sender ID
MTN_TARGET_ENVIRONMENT=sandbox  # Use "sandbox" for testing, "production" for live
```

### Optional Variables

```bash
MTN_SENDER_NAME=MTN  # Display name (defaults to MTN if not set)
MTN_NOTIFY_URL=https://your-callback-url.com/dlr  # Webhook for delivery receipts
MTN_CALLBACK_DATA=custom-data  # Custom data to include in webhook callbacks
```

## Step 5: Configure in Supabase Dashboard

1. Go to your Supabase project dashboard
2. Navigate to **Edge Functions** → **send-notification**
3. Click on **Settings** or **Configuration**
4. Add the environment variables from Step 4
5. Save the configuration
6. Redeploy the function if it's already deployed

## Step 6: Deploy Edge Function

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

## Step 7: Test Configuration

### Test OAuth Token Generation

The function automatically handles OAuth 2.0 token generation. Check the logs:

```bash
# View function logs in Supabase Dashboard
# Or via CLI:
supabase functions logs send-notification
```

You should see:
```
MTN access token obtained and cached
```

### Test SMS Sending

Use the notification API to send a test SMS:

```bash
curl -X POST https://YOUR_PROJECT.supabase.co/functions/v1/send-notification \
  -H "Authorization: Bearer YOUR_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "TEST",
    "message": "Test message from MTN SMS API",
    "recipientPhoneNumbers": ["+250788123456"]
  }'
```

### Expected Response

```json
{
  "success": true,
  "message": "Notifications processed: 1 sent, 0 failed",
  "sentCount": 1,
  "failedCount": 0
}
```

## API Architecture

### OAuth 2.0 Authentication

The implementation uses **Client Credentials Flow**:

1. **Token Endpoint**: `POST https://api.mtn.com/v1/oauth/access_token/accesstoken`
2. **Grant Type**: `client_credentials`
3. **Scope**: `SEND-SMS`
4. **Authentication**: Basic Auth (Base64 encoded `ConsumerKey:ConsumerSecret`)

### Access Token Management

- Tokens are automatically cached in memory
- Tokens are refreshed 5 minutes before expiration
- Default expiration: 1 hour (3600 seconds)
- Cache is per-function instance (consider Redis for multi-instance deployments)

### Send SMS Endpoint

**Endpoint**: `POST https://api.mtn.com/v3/sms/messages/sms/outbound`

**Request Format**:
```json
{
  "outboundSMSMessageRequest": {
    "senderAddress": "MTN",
    "receiverAddress": ["tel:+250788123456"],
    "outboundSMSTextMessage": {
      "message": "Your message text here"
    },
    "clientCorrelator": "unique-request-id",
    "receiptRequest": {
      "notifyURL": "https://your-webhook.com/dlr",
      "callbackData": "optional-data"
    },
    "senderName": "MTN"
  }
}
```

## Phone Number Format

MTN API requires phone numbers in **E.164 format with `tel:` prefix**:

- ✅ `tel:+250788123456` (correct format)
- ✅ `+250788123456` (auto-formatted to `tel:+250788123456`)
- ❌ `0788123456` (missing country code)
- ❌ `250788123456` (missing `+` and `tel:` prefix)

The edge function automatically formats phone numbers to the required format.

## Delivery Receipts (Webhooks)

To receive delivery status updates, configure a webhook endpoint:

1. Set `MTN_NOTIFY_URL` to your callback endpoint
2. MTN will POST delivery status updates to this URL
3. Implement the webhook handler to process delivery receipts

### Webhook Payload Example

```json
{
  "deliveryInfo": [
    {
      "address": "tel:+250788123456",
      "deliveryStatus": "DeliveredToTerminal"
    }
  ],
  "resourceReference": {
    "resourceURL": "https://api.mtn.com/v3/sms/messages/sms/outbound/..."
  }
}
```

### Delivery Status Values

- `DeliveredToTerminal`: Message delivered to recipient's device
- `DeliveryUncertain`: Delivery status unknown
- `DeliveryImpossible`: Message could not be delivered
- `MessageWaiting`: Message queued for delivery

## Rate Limits

- **Default**: ~100 requests per minute
- Varies by account tier and country
- Implement request queuing if you expect high volume

## Error Handling

Common error codes and solutions:

### 401 Unauthorized
- **Cause**: Invalid Consumer Key/Secret or expired token
- **Solution**: Verify credentials, check token refresh logic

### 400 Bad Request
- **Cause**: Invalid phone number format, missing required fields
- **Solution**: Verify phone number format, check request payload

### 403 Forbidden
- **Cause**: Sender ID not whitelisted, insufficient permissions
- **Solution**: Request Sender ID approval, check application permissions

### 429 Too Many Requests
- **Cause**: Rate limit exceeded
- **Solution**: Implement request throttling, use request queue

## Testing Checklist

- [ ] MTN Developer Portal account created
- [ ] Application created and Consumer Key/Secret obtained
- [ ] Sender ID whitelisting requested (for production)
- [ ] Environment variables configured in Supabase
- [ ] Edge function deployed
- [ ] OAuth token generation working (check logs)
- [ ] Test SMS sent successfully
- [ ] Delivery receipt webhook configured (optional)
- [ ] Error handling tested
- [ ] Rate limiting considered for production

## Production Considerations

1. **Sender ID Approval**: Allow 1-4 weeks for approval depending on country
2. **Token Storage**: Consider Redis or database for token caching in multi-instance deployments
3. **Error Monitoring**: Set up alerts for failed SMS sends
4. **Delivery Tracking**: Implement webhook handlers for delivery receipts
5. **Rate Limiting**: Monitor usage and implement queuing if needed
6. **Cost Monitoring**: Track SMS usage and costs per country
7. **Backup Provider**: Consider MessageBird or Generic API as fallback

## Support & Resources

- **MTN Developer Portal**: https://developer.mtn.com
- **API Documentation**: https://developer.mtn.com/api-documentation
- **Support**: Contact MTN Developer Support through the portal
- **Status Page**: Check MTN API status for outages

## Security Best Practices

1. **Credentials**: Never commit Consumer Key/Secret to version control
2. **Environment Variables**: Use Supabase secrets management
3. **HTTPS**: Always use HTTPS for webhooks and API calls
4. **Token Security**: Tokens are automatically cached securely in memory
5. **Rate Limiting**: Implement client-side rate limiting to avoid abuse

---

**Need Help?** Check the edge function logs in Supabase Dashboard or contact MTN Developer Support.
