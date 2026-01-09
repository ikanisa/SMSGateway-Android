# SMS Gateway Android - Action Plan

## 🚨 Critical Security Fixes (Do First)

### 1. Move Hardcoded Secrets
- [ ] Create `local.properties` (gitignored) with Supabase credentials
- [ ] Update `build.gradle.kts` to read from `local.properties`
- [ ] Update `AppDefaults.kt` to use `BuildConfig` fields
- [ ] Remove hardcoded secrets from source code
- [ ] Document secure build process

**Files to modify:**
- `app/build.gradle.kts`
- `app/src/main/java/com/example/smsgateway/AppDefaults.kt`
- `.gitignore` (ensure `local.properties` is ignored)

### 2. Encrypt Sensitive Data
- [ ] Add `androidx.security:security-crypto` dependency
- [ ] Create `SecurePreferences` wrapper class
- [ ] Replace all `SharedPreferences` usage with `EncryptedSharedPreferences`
- [ ] Migrate existing data (if any)

**Files to create:**
- `app/src/main/java/com/example/smsgateway/data/SecurePreferences.kt`

**Files to modify:**
- `ProcessSmsWorker.kt`
- `DeviceLookupHelper.kt`
- `MainActivity.kt`
- `SettingsScreen.kt`

### 3. Remove Static ViewModel Reference
- [ ] Create `SmsRepository` interface
- [ ] Implement `SmsRepositoryImpl`
- [ ] Remove static ViewModel from `ProcessSmsWorker`
- [ ] Use StateFlow/LiveData for communication
- [ ] Update ViewModel to use Repository

**Files to create:**
- `app/src/main/java/com/example/smsgateway/data/SmsRepository.kt`
- `app/src/main/java/com/example/smsgateway/data/SmsRepositoryImpl.kt`

**Files to modify:**
- `ProcessSmsWorker.kt`
- `MainViewModel.kt`

---

## 🎨 UI/UX Modernization

### 4. Complete Compose Migration
- [ ] Remove `SettingsActivity.kt` (XML-based)
- [ ] Ensure `SettingsScreen.kt` (Compose) is fully functional
- [ ] Update navigation to use only Compose screens
- [ ] Remove unused XML layouts
- [ ] Test all settings functionality

**Files to delete:**
- `app/src/main/java/com/example/smsgateway/SettingsActivity.kt`
- `app/src/main/res/layout/activity_settings.xml`
- `app/src/main/res/layout/dialog_gateway_config.xml`

**Files to modify:**
- `app/src/main/AndroidManifest.xml` (remove SettingsActivity)
- `MainActivity.kt` (navigation)

### 5. Implement Glassmorphism Design
- [ ] Create `GlassCard` composable component
- [ ] Create `GlassSurface` composable
- [ ] Update all cards to use glassmorphism
- [ ] Add blur effects and transparency
- [ ] Test on light and dark themes

**Files to create:**
- `app/src/main/java/com/example/smsgateway/ui/components/GlassCard.kt`

**Files to modify:**
- `StatusCards.kt`
- `Cards.kt`
- `HomeScreen.kt`
- `SettingsScreen.kt`

### 6. Minimalist Design Improvements
- [ ] Reduce visual clutter (remove unnecessary borders)
- [ ] Increase spacing between elements (16dp → 24dp)
- [ ] Simplify color palette
- [ ] Improve typography hierarchy
- [ ] Add more white space

**Files to modify:**
- All UI components
- `Color.kt` (simplify palette)
- `Theme.kt` (update spacing)

### 7. Enhanced Status Indicators
- [ ] Add icon-based status (not just dots)
- [ ] Show last sync time
- [ ] Add connection quality indicator
- [ ] Improve animations
- [ ] Add haptic feedback

**Files to modify:**
- `StatusCards.kt`
- `HomeScreen.kt`

---

## 🏗️ Architecture Improvements

### 8. Implement Repository Pattern
- [ ] Create `SmsRepository` interface
- [ ] Implement `SmsRepositoryImpl`
- [ ] Create `SupabaseApi` data source
- [ ] Create `SmsLocalDataSource` for offline support
- [ ] Update ViewModel to use Repository
- [ ] Update Worker to use Repository

**Files to create:**
- `app/src/main/java/com/example/smsgateway/data/repository/SmsRepository.kt`
- `app/src/main/java/com/example/smsgateway/data/repository/SmsRepositoryImpl.kt`
- `app/src/main/java/com/example/smsgateway/data/datasource/SupabaseApi.kt`
- `app/src/main/java/com/example/smsgateway/data/datasource/SmsLocalDataSource.kt`

### 9. Add Dependency Injection
- [ ] Add Hilt dependencies
- [ ] Create `Application` class with `@HiltAndroidApp`
- [ ] Create DI modules
- [ ] Inject dependencies in Activities/ViewModels
- [ ] Remove manual dependency creation

**Files to create:**
- `app/src/main/java/com/example/smsgateway/SmsGatewayApplication.kt`
- `app/src/main/java/com/example/smsgateway/di/AppModule.kt`
- `app/src/main/java/com/example/smsgateway/di/NetworkModule.kt`
- `app/src/main/java/com/example/smsgateway/di/RepositoryModule.kt`

**Files to modify:**
- `AndroidManifest.xml` (add Application)
- All Activities and ViewModels

### 10. Improve Error Handling
- [ ] Create sealed class for error states
- [ ] Implement retry mechanisms
- [ ] Add user-friendly error messages
- [ ] Implement exponential backoff
- [ ] Add offline queue

**Files to create:**
- `app/src/main/java/com/example/smsgateway/data/Result.kt`
- `app/src/main/java/com/example/smsgateway/data/ErrorHandler.kt`

---

## 🔧 Code Quality

### 11. Fix Package Name Inconsistency
- [ ] Update all source files from `com.example.smsgateway` to `com.ikanisa.smsgateway`
- [ ] Update imports
- [ ] Test build

**Files to modify:**
- All `.kt` files in `app/src/main/java/com/example/smsgateway/`

### 12. Modernize Date/Time Handling
- [ ] Replace `SimpleDateFormat` with `java.time`
- [ ] Use ThreeTenABP for API < 26
- [ ] Create utility functions for date formatting

**Files to create:**
- `app/src/main/java/com/example/smsgateway/util/DateUtils.kt`

**Files to modify:**
- `ProcessSmsWorker.kt`

### 13. Implement Structured Logging
- [ ] Add Timber dependency
- [ ] Replace StringBuilder logs with Timber
- [ ] Implement log rotation
- [ ] Add Firebase Crashlytics integration
- [ ] Remove debug logs in release builds

**Files to modify:**
- `MainViewModel.kt`
- `ProcessSmsWorker.kt`
- `SmsReceiver.kt`

### 14. Extract Constants
- [ ] Create `Config` object for network timeouts
- [ ] Create `Constants` object for app-wide constants
- [ ] Remove magic numbers

**Files to create:**
- `app/src/main/java/com/example/smsgateway/util/Config.kt`
- `app/src/main/java/com/example/smsgateway/util/Constants.kt`

---

## 📱 Performance & Optimization

### 15. Memory Management
- [ ] Implement log rotation (keep last 100 entries)
- [ ] Use Room database for persistent logs
- [ ] Remove unbounded StringBuilder growth

**Files to create:**
- `app/src/main/java/com/example/smsgateway/data/local/LogDatabase.kt`
- `app/src/main/java/com/example/smsgateway/data/local/LogDao.kt`

### 16. Network Optimization
- [ ] Implement request batching
- [ ] Add request queuing
- [ ] Implement response caching where appropriate

### 17. Battery Optimization
- [ ] Add WorkManager constraints
- [ ] Implement adaptive sync intervals
- [ ] Reduce wake locks

---

## 🧪 Testing

### 18. Add Unit Tests
- [ ] Test ViewModel logic
- [ ] Test Repository methods
- [ ] Test utility functions
- [ ] Set up test coverage reporting

**Files to create:**
- `app/src/test/java/com/example/smsgateway/MainViewModelTest.kt`
- `app/src/test/java/com/example/smsgateway/SmsRepositoryTest.kt`

### 19. Add Integration Tests
- [ ] Test SMS reception flow
- [ ] Test API communication
- [ ] Test Worker execution

### 20. Add UI Tests
- [ ] Test Compose UI
- [ ] Test navigation flows
- [ ] Test user interactions

---

## 📚 Documentation

### 21. Code Documentation
- [ ] Add KDoc to all public functions
- [ ] Document architecture decisions
- [ ] Create architecture diagram

### 22. User Documentation
- [ ] Update README with setup instructions
- [ ] Add troubleshooting guide
- [ ] Create FAQ

### 23. Developer Documentation
- [ ] Add contributing guidelines
- [ ] Create code style guide
- [ ] Document build process

---

## 🔒 Security Enhancements

### 24. Certificate Pinning
- [ ] Implement certificate pinning for Supabase
- [ ] Test with OkHttp CertificatePinner
- [ ] Handle certificate rotation

**Files to modify:**
- `ProcessSmsWorker.kt`
- `DeviceLookupHelper.kt`

### 25. BroadcastReceiver Security
- [ ] Review `SmsReceiver` export settings
- [ ] Add permission checks
- [ ] Validate SMS format

**Files to modify:**
- `AndroidManifest.xml`
- `SmsReceiver.kt`

---

## 📦 Dependencies

### 26. Update Dependencies
- [ ] Review and update all dependencies
- [ ] Add security library
- [ ] Add dependency injection
- [ ] Add logging library
- [ ] Add testing libraries

**Files to modify:**
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`

---

## ✅ Quick Wins (Can Do Immediately)

1. **Fix package name** - 30 minutes
2. **Extract constants** - 1 hour
3. **Add Timber logging** - 1 hour
4. **Update README** - 30 minutes
5. **Remove unused XML layouts** - 15 minutes

---

## 📊 Estimated Effort

| Category | Estimated Time |
|----------|---------------|
| Critical Security Fixes | 2-3 days |
| UI/UX Modernization | 3-5 days |
| Architecture Improvements | 5-7 days |
| Code Quality | 2-3 days |
| Testing | 3-5 days |
| Documentation | 1-2 days |
| **Total** | **16-25 days** |

---

## 🎯 Recommended Sprint Plan

### Sprint 1 (Week 1): Security & Critical Fixes
- Move hardcoded secrets
- Encrypt sensitive data
- Remove static ViewModel reference
- Fix package name

### Sprint 2 (Week 2): Architecture
- Implement Repository pattern
- Add dependency injection
- Improve error handling

### Sprint 3 (Week 3): UI/UX
- Complete Compose migration
- Implement glassmorphism
- Minimalist design improvements

### Sprint 4 (Week 4): Quality & Testing
- Add unit tests
- Add integration tests
- Code documentation
- Performance optimization

---

## 📝 Notes

- Prioritize security fixes first
- Test thoroughly after each major change
- Keep backups before refactoring
- Document all architectural decisions
- Review code with team before merging

---

**Last Updated:** January 2025
