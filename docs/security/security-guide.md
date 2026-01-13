# Security Guide

## Overview

This guide outlines security best practices implemented in the SMS Gateway Android application.

## Credential Storage

### EncryptedSharedPreferences
All sensitive data is stored using Android's EncryptedSharedPreferences:

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val prefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### Credentials Managed
- Supabase URL and API keys
- MOMO activation codes
- FCM tokens

## API Security

### Authentication Headers
All Supabase API calls include:
- `apikey`: Supabase anon key
- `Authorization`: Bearer token

### HTTPS Enforcement
- All network communication uses TLS 1.2+
- Certificate pinning recommended for production

## Data Protection

### Sensitive Permissions
| Permission | Purpose | Protection |
|------------|---------|------------|
| `RECEIVE_SMS` | Capture incoming SMS | Runtime permission required |
| `READ_SMS` | Read SMS content | Runtime permission required |
| `INTERNET` | Send to backend | Secured by TLS |

### Data Minimization
- SMS content is processed and forwarded, not stored locally
- Only essential metadata is retained

## Build Security

### ProGuard/R8
- Code obfuscation enabled in release builds
- Serialization and Hilt classes preserved
- Crash reports include line numbers

### Keystore
- Release builds use a secure keystore
- Keystore credentials stored outside version control

## Recommendations

1. **Rotate API Keys** regularly via Supabase dashboard
2. **Enable 2FA** on Firebase and Supabase accounts
3. **Monitor Crashlytics** for security-related crashes
4. **Use App Check** for Firebase API protection
