# Build and Test Guide

## 🚀 Quick Start

### Prerequisites

1. **Set up local.properties:**
   ```bash
   cp local.properties.template local.properties
   ```
   
2. **Edit local.properties with your Supabase credentials:**
   ```properties
   supabase.url=https://your-project-id.supabase.co
   supabase.key=your-anon-key-here
   ```

### Build Commands

#### Debug Build
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

#### Release Build
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

#### Clean Build
```bash
./gradlew clean assembleDebug
```

### Test Commands

#### Run All Tests
```bash
./gradlew test
```

#### Run Unit Tests Only
```bash
./gradlew testDebugUnitTest
```

#### Run Tests with Coverage
```bash
./gradlew testDebugUnitTestCoverage
```
Coverage report: `app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html`

#### Run Specific Test Class
```bash
./gradlew test --tests "com.example.smsgateway.MainViewModelTest"
```

### Verification Steps

#### 1. Verify Hilt Injection

**Check Application Class:**
- Verify `SmsGatewayApplication` is annotated with `@HiltAndroidApp`
- Check `AndroidManifest.xml` has `android:name=".SmsGatewayApplication"`

**Check Components:**
- `MainActivity` should have `@AndroidEntryPoint`
- `MainViewModel` should have `@HiltViewModel` and `@Inject` constructor
- `ProcessSmsWorker` should have `@HiltWorker`

**Build Test:**
```bash
./gradlew assembleDebug
```
If build succeeds, Hilt is configured correctly.

#### 2. Verify Glassmorphism UI

**Visual Check:**
1. Build and install the app
2. Open the app
3. Check that cards have:
   - Frosted glass effect
   - Transparent/translucent background
   - Smooth animations
   - Modern minimalist design

**Code Check:**
- Verify `GlassCard.kt` exists
- Check `StatusCard` and `MetricCard` use `GlassCard`
- Import statements should include `com.example.smsgateway.ui.components.GlassCard`

#### 3. Verify Compose Migration

**Check:**
- `SettingsActivity.kt` should be deleted
- `activity_settings.xml` should be deleted
- `AndroidManifest.xml` should not reference `SettingsActivity`
- All settings should be accessible via Compose `SettingsScreen`

**Test:**
1. Open app
2. Tap Settings icon
3. Verify settings screen opens (Compose-based)
4. Verify all settings work correctly

### Common Issues and Fixes

#### Issue: Build fails with "Cannot find symbol: BuildConfig"
**Fix:** 
- Ensure `local.properties` exists with Supabase credentials
- Rebuild: `./gradlew clean build`

#### Issue: Hilt injection fails
**Fix:**
- Verify `@HiltAndroidApp` on Application class
- Check all modules are properly annotated
- Ensure `kapt` plugin is applied
- Clean and rebuild: `./gradlew clean build`

#### Issue: Worker not receiving dependencies
**Fix:**
- Verify `@HiltWorker` annotation
- Check `SmsGatewayApplication` implements `Configuration.Provider`
- Verify WorkManager is initialized with Hilt factory

#### Issue: Glassmorphism not showing
**Fix:**
- Check `GlassCard.kt` is imported correctly
- Verify Material 3 theme is applied
- Check for compilation errors in UI components

### Testing Checklist

- [ ] App builds successfully (`./gradlew assembleDebug`)
- [ ] All tests pass (`./gradlew test`)
- [ ] Hilt injection works (no runtime errors)
- [ ] Glassmorphism UI displays correctly
- [ ] Settings screen works (Compose-based)
- [ ] SMS reception and forwarding works
- [ ] Counters update correctly
- [ ] Logs display properly

### Performance Checks

```bash
# Check build time
time ./gradlew assembleDebug

# Check APK size
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Check test execution time
time ./gradlew test
```

### Continuous Integration

For CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Build
  run: ./gradlew assembleDebug

- name: Test
  run: ./gradlew test

- name: Generate Coverage
  run: ./gradlew testDebugUnitTestCoverage
```

### Next Steps After Build

1. **Install on device:**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Run app and verify:**
   - All features work
   - UI looks correct
   - No crashes
   - SMS forwarding works

3. **Check logs:**
   ```bash
   adb logcat | grep -i smsgateway
   ```

---

**Last Updated:** January 2025
