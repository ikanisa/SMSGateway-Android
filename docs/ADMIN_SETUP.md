# SMS Gateway Admin Setup Guide

This guide covers how to set up and manage the SMS Gateway system for administrators.

## Prerequisites

- Firebase project with Crashlytics, Analytics, Cloud Messaging
- Supabase project with Edge Functions deployed
- Android device(s) for SMS receiving

---

## 1. Firebase Setup

### 1.1 Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create new project or use existing
3. Add Android app with package name: `com.ikanisa.smsgateway`
4. Download `google-services.json`

### 1.2 Enable Services
- **Crashlytics**: Project Settings → Crashlytics → Enable
- **Analytics**: Automatically enabled
- **Cloud Messaging**: Project Settings → Cloud Messaging → Enable

### 1.3 App Distribution
1. Go to App Distribution
2. Add tester emails
3. Create tester group "testers"

---

## 2. Supabase Setup

### 2.1 Tables Required
```sql
-- Device registry
CREATE TABLE device_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id TEXT UNIQUE NOT NULL,
    device_secret TEXT NOT NULL,
    device_label TEXT,
    momo_msisdn TEXT,
    momo_code TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- SMS transactions
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id TEXT NOT NULL,
    sender TEXT,
    body TEXT,
    received_at TIMESTAMPTZ,
    parse_status TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);
```

### 2.2 Edge Functions
Deploy the `ingest-sms` and `lookup-device` functions from `supabase/functions/`.

---

## 3. Device Provisioning

### 3.1 Add Device to Registry
```sql
INSERT INTO device_keys (device_id, device_secret, device_label, momo_msisdn, momo_code)
VALUES (
    'device-001',
    'secure-secret-here',
    'Main Phone',
    '+250788123456',
    '1234'
);
```

### 3.2 Install App on Device
1. Download APK from Firebase App Distribution
2. Enable "Install from unknown sources"
3. Install and grant SMS permissions
4. Enter MoMo number in Settings

---

## 4. GitHub CI/CD Secrets

Add these secrets in repository Settings → Secrets:

| Secret | How to Get |
|--------|-----------|
| `GOOGLE_SERVICES_JSON` | Copy entire contents of google-services.json |
| `SUPABASE_URL` | Supabase Dashboard → Settings → API |
| `SUPABASE_ANON_KEY` | Supabase Dashboard → Settings → API |
| `KEYSTORE_BASE64` | `base64 -i release.keystore` |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | Key alias in keystore |
| `KEY_PASSWORD` | Key password |
| `FIREBASE_APP_ID` | Firebase Console → Project Settings |
| `FIREBASE_SERVICE_ACCOUNT` | Firebase Console → Service Accounts → Generate Key |

---

## 5. Monitoring

### Crashlytics Dashboard
- Monitor crash-free users percentage
- Set up alerts for new crash types

### Analytics Events
- `sms_received` - SMS forwarded to backend
- `sms_error` - Processing error occurred
- `device_activated` - New device provisioned

---

## 6. Troubleshooting

| Issue | Solution |
|-------|----------|
| App not receiving SMS | Check SMS permissions in device settings |
| SMS not forwarding | Verify device is in `device_keys` table |
| Build fails in CI | Check secrets are properly configured |
| Firebase deploy fails | Verify service account has correct permissions |

---

## Support

For issues, check:
1. Firebase Crashlytics for app crashes
2. Supabase logs for backend errors
3. GitHub Actions for CI/CD failures
