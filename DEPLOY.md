# SMS Gateway Deployment Guide

## Overview
This project uses **Gradle** as the build system. Do not use `npm` or `node` based commands for this Android project.

## Prerequisites
1.  **Java 17+** installed.
2.  **Firebase CLI** installed and logged in (`firebase login`), OR set `GOOGLE_APPLICATION_CREDENTIALS` environment variable.
3.  **Android SDK** setup.

## Commands

### 1. Build Release APK
Generates the signed APK in `app/build/outputs/apk/release/`.

```bash
./gradlew assembleRelease
```

### 2. Deploy to Firebase App Distribution
Builds the APK and uploads it to Firebase App Distribution, notifying the `testers` group.

```bash
./gradlew appDistributionUploadRelease
```

### Common Issues

**"Task 'appDistributionUploadRelease' not found"**
- Ensure you have synchronized Gradle: `./gradlew clean` or "Sync Project with Gradle Files" in Android Studio.

**"Authentication Error"**
- Run `firebase login` on your terminal.
- Or use a service account key for CI/CD.

## Testing
To run unit tests:
```bash
./gradlew test
```
