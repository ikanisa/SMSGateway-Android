# SMS Gateway Android

An Android application that receives SMS messages and forwards them to a Supabase Edge Function for parsing and storage using Gemini AI.

## Features

- 📱 Receives incoming SMS messages via BroadcastReceiver
- 🔄 Background processing using WorkManager
- 🤖 Gemini AI-powered SMS parsing (MoMo/banking transactions)
- ☁️ Supabase backend integration
- 🎨 Modern Material 3 UI with Jetpack Compose

## Setup

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11+
- Firebase project (for App Distribution)

### 1. Firebase Configuration

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
2. Add an Android app with package name `com.example.smsgateway`
3. Download `google-services.json` and place it in `app/` directory

### 2. Release Signing

To build release APKs, create a release keystore:

```bash
keytool -genkey -v -keystore release.keystore -alias smsgateway -keyalg RSA -keysize 2048 -validity 10000
```

Then create `keystore.properties` at the project root (copy from template):

```bash
cp keystore.properties.template keystore.properties
```

Edit `keystore.properties` with your keystore credentials.

### 3. App Configuration

Users must configure the following in the app's Settings screen:
- Supabase URL
- Supabase Anon Key
- Device ID
- Device Secret

## Building

### Debug Build

```bash
./gradlew assembleDebug
```

### Release Build

```bash
./gradlew assembleRelease
```

The APK will be at `app/build/outputs/apk/release/app-release.apk`

## Firebase App Distribution

1. Install Firebase CLI: `npm install -g firebase-tools`
2. Login: `firebase login`
3. Upload:
   ```bash
   firebase appdistribution:distribute app/build/outputs/apk/release/app-release.apk \
     --app YOUR_FIREBASE_APP_ID \
     --groups "internal-testers"
   ```

## Architecture

```
app/src/main/java/com/example/smsgateway/
├── MainActivity.kt          # Main entry point with Compose UI
├── MainViewModel.kt          # UI state management
├── SmsReceiver.kt            # BroadcastReceiver for SMS
├── ProcessSmsWorker.kt       # WorkManager worker for API calls
├── AppDefaults.kt            # Default configuration
└── ui/
    ├── screens/              # Compose screens
    ├── components/           # Reusable UI components
    └── theme/                # Material 3 theming
```

## Backend

The Supabase Edge Function (`supabase/functions/ingest-sms/`) handles:
- Device authentication
- SMS storage
- Gemini AI parsing for transaction extraction

## License

Proprietary - Internal use only
