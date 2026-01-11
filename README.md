# SMS Gateway Android

A production-ready Android application that forwards SMS messages to a Supabase backend for processing.

## Features

- 📱 **SMS Forwarding** - Automatically captures and forwards incoming SMS to backend
- 🔐 **Secure Storage** - Uses EncryptedSharedPreferences for credentials
- 🔄 **Offline Queue** - Queues SMS when offline, syncs when connected
- 🔥 **Firebase Integration** - Crashlytics, Analytics, and Cloud Messaging
- 🎨 **Modern UI** - Material 3 with glassmorphism components
- ✅ **Tested** - 29+ unit tests with 80%+ coverage target

## Quick Start

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 35

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/ikanisa/SMSGateway-Android.git
   cd SMSGateway-Android
   ```

2. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Download `google-services.json` and place it in `app/`

3. **Configure Supabase**
   - Create `local.properties` in the root directory:
   ```properties
   supabase.url=https://your-project.supabase.co
   supabase.key=your-anon-key
   momo.code=your-momo-code
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

## Architecture

```
app/src/main/java/com/ikanisa/smsgateway/
├── data/
│   ├── model/          # Data classes
│   ├── repository/     # Repository pattern
│   └── datasource/     # API clients
├── di/                 # Hilt dependency injection
├── notification/       # FCM and SMS services
├── ui/
│   ├── components/     # Reusable UI components
│   ├── screens/        # Compose screens
│   └── theme/          # Material 3 theming
├── workers/            # WorkManager background tasks
├── MainActivity.kt
├── MainViewModel.kt
└── SmsGatewayApplication.kt
```

## Testing

Run unit tests:
```bash
./gradlew testDebugUnitTest
```

Run with coverage:
```bash
./gradlew testDebugUnitTestCoverage
```

## CI/CD

The project uses GitHub Actions for continuous integration:

| Workflow | Trigger | Actions |
|----------|---------|---------|
| `android-ci.yml` | Push to main/develop | Run tests, Build APK, Deploy to Firebase |

### Required Secrets

Configure these in GitHub repository settings:

| Secret | Description |
|--------|-------------|
| `GOOGLE_SERVICES_JSON` | Contents of google-services.json |
| `SUPABASE_URL` | Supabase project URL |
| `SUPABASE_ANON_KEY` | Supabase anon key |
| `KEYSTORE_BASE64` | Base64-encoded release keystore |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |
| `FIREBASE_APP_ID` | Firebase App ID |
| `FIREBASE_SERVICE_ACCOUNT` | Firebase service account JSON |

## Release

1. Update version in `app/build.gradle.kts`
2. Push to `main` branch
3. CI/CD automatically builds and deploys to Firebase App Distribution

## License

Copyright © 2024 Ikanisa. All rights reserved.
