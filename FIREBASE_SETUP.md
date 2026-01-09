# Firebase Cloud Messaging (FCM) Setup Guide

**⚠️ IMPORTANT**: Firebase project is **ALREADY CREATED** and configured for this app. This is an **UPDATE** guide for maintaining the existing setup.

## Current Status

✅ Firebase project exists and is configured
✅ `google-services.json` is already in place (check `app/` directory)
✅ FCM token registration is implemented in `SmsGatewayApplication.kt`
✅ FCM messaging service is configured in `AppFirebaseMessagingService.kt`

## If google-services.json is Missing

If `app/google-services.json` is not present, download it from your existing Firebase project:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your existing project
3. Go to **Project Settings** (gear icon) → **Your apps**
4. Find the Android app with package name `com.ikanisa.smsgateway`
5. Click **Download google-services.json**
6. Place it in the `app/` directory:
   ```
   SMSGateway-Android/
   └── app/
       └── google-services.json
   ```

## Existing Configuration

The app already has Firebase Cloud Messaging configured:

- **Application Class**: `SmsGatewayApplication.kt` - Auto-registers FCM tokens
- **Messaging Service**: `AppFirebaseMessagingService.kt` - Handles push notifications
- **Manifest**: `AndroidManifest.xml` - FCM service declared
- **Dependencies**: Firebase BOM, Messaging, Analytics, Crashlytics in `build.gradle.kts`

## Verification

To verify Firebase is working:

1. Check logcat for FCM token registration:
   ```bash
   adb logcat | grep -E "(SmsGatewayApplication|FirebaseMessagingService|FcmTokenRegistration)"
   ```
   
2. You should see:
   ```
   D/SmsGatewayApplication: FCM token obtained: ...
   D/FcmTokenRegistration: FCM token registered successfully
   ```

3. Check Supabase `fcm_tokens` table for registered tokens

## Cloud Messaging Settings (Already Configured)

- **FCM API**: Already enabled
- **Token Registration**: Automatic on app startup
- **Push Notifications**: Ready to receive via `AppFirebaseMessagingService`

## Step 6: Test FCM Token Registration

Once `google-services.json` is in place, the app will automatically:

1. Get FCM token on first launch
2. Register token with Supabase
3. Log token to logcat (filter by "SmsGatewayApplication" or "FirebaseMessagingService")

### Verify Token Registration

Check logcat:
```bash
adb logcat | grep -E "(SmsGatewayApplication|FirebaseMessagingService|FcmTokenRegistration)"
```

You should see:
```
D/SmsGatewayApplication: FCM token obtained: ...
D/FcmTokenRegistration: FCM token registered successfully
```

## Step 7: Create FCM Tokens Table in Supabase (Optional but Recommended)

To store FCM tokens for push notification targeting:

```sql
-- Migration: Create FCM tokens table
CREATE TABLE IF NOT EXISTS fcm_tokens (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  fcm_token TEXT NOT NULL UNIQUE,
  device_id TEXT,
  momo_code TEXT,
  user_id UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_momo_code ON fcm_tokens(momo_code);
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_user_id ON fcm_tokens(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_device_id ON fcm_tokens(device_id) WHERE device_id IS NOT NULL;

-- RLS Policies
ALTER TABLE fcm_tokens ENABLE ROW LEVEL SECURITY;

-- Allow service role full access
CREATE POLICY "Service role full access on fcm_tokens" ON fcm_tokens
  FOR ALL USING (auth.role() = 'service_role');

-- Allow anon users to insert/update their own tokens
CREATE POLICY "Anon users can manage their tokens" ON fcm_tokens
  FOR ALL USING (true); -- Adjust based on your security requirements

-- Updated_at trigger
CREATE OR REPLACE FUNCTION update_fcm_tokens_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_fcm_tokens_updated_at_trigger
  BEFORE UPDATE ON fcm_tokens
  FOR EACH ROW
  EXECUTE FUNCTION update_fcm_tokens_updated_at();
```

## Step 8: Create Edge Function for Token Registration (Optional)

If direct table access doesn't work, create an edge function:

```typescript
// supabase/functions/register-fcm-token/index.ts
import { createClient } from "npm:@supabase/supabase-js@2";

Deno.serve(async (req) => {
  const { fcm_token, device_id, momo_code, user_id } = await req.json();
  
  const supabase = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
  );
  
  const { data, error } = await supabase
    .from("fcm_tokens")
    .upsert({
      fcm_token,
      device_id,
      momo_code,
      user_id,
      updated_at: new Date().toISOString(),
    }, {
      onConflict: "fcm_token"
    });
  
  if (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
  
  return new Response(JSON.stringify({ success: true }), {
    headers: { "Content-Type": "application/json" },
  });
});
```

## Step 9: Send Test Push Notification

### From Firebase Console

1. Go to **Cloud Messaging** → **Send your first message**
2. Enter notification title and text
3. Select **Send test message**
4. Enter FCM token (from logcat)
5. Click **Test**

### From Server/Backend

Use Firebase Admin SDK or HTTP API to send:

```bash
curl -X POST https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": {
      "token": "FCM_TOKEN_HERE",
      "data": {
        "action": "send_daily_reminders"
      }
    }
  }'
```

## Troubleshooting

### Token Not Registered

1. Check `google-services.json` is in `app/` directory
2. Verify package name matches: `com.ikanisa.smsgateway`
3. Check internet connection
4. Verify Supabase URL and keys are correct

### Push Notifications Not Received

1. Check app has notification permissions (Android 13+)
2. Verify FCM token is valid (check Firebase Console)
3. Check device is not in battery optimization/doze mode
4. Verify `AppFirebaseMessagingService` is properly registered in AndroidManifest

### Build Errors

If you see errors about `google-services.json`:

1. Ensure file is in `app/` directory (not `app/src/main/`)
2. Verify JSON syntax is valid
3. Clean and rebuild project: `./gradlew clean build`
4. Sync Gradle files in Android Studio

## Security Best Practices

1. **Don't commit `google-services.json`** if it contains sensitive data
   - Add to `.gitignore`
   - Use build variants or environment variables for different environments

2. **Use different Firebase projects** for development and production

3. **Restrict API keys** in Google Cloud Console:
   - Go to Google Cloud Console → APIs & Services → Credentials
   - Restrict Android apps by package name and SHA-1

4. **Monitor FCM usage** in Firebase Console to detect abuse

## Production Checklist

- [ ] `google-services.json` added to `app/` directory
- [ ] FCM token registration working (check logs)
- [ ] Test push notification received
- [ ] `fcm_tokens` table created in Supabase (if using)
- [ ] Edge function deployed (if using)
- [ ] SHA-1 fingerprint added to Firebase (for release builds)
- [ ] Production Firebase project configured (separate from dev)
- [ ] API keys restricted in Google Cloud Console
- [ ] Notification permissions handled in app (Android 13+)

## Additional Resources

- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [FCM Android Setup Guide](https://firebase.google.com/docs/cloud-messaging/android/client)
- [Supabase Edge Functions](https://supabase.com/docs/guides/functions)
