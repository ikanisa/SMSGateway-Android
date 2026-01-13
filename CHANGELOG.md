# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Organized documentation structure in `docs/` directory
- New DI modules: `AppModule`, `DatabaseModule`, `WorkerModule`
- Domain layer package structure (`domain/model/`, `domain/repository/`, `domain/usecase/`)
- Infrastructure layer packages for receivers, workers, and services
- Utility classes: `Constants.kt` and `Extensions.kt`
- Security documentation and system architecture guide

### Changed
- Optimized `gradle.properties` for faster builds (4GB heap, parallel, caching)
- Reorganized documentation files into proper `docs/` subdirectories

### Removed
- 30+ development status and temporary markdown files from root
- `.res_backups/` directory with obsolete resource backups

## [1.0.4] - 2025-12-15

### Added
- Modern glassmorphism UI components
- Firebase Cloud Messaging integration
- Daily reminder notifications
- Device activation with MOMO code

### Fixed
- ProGuard/R8 configuration for release builds
- Kotlinx Serialization compatibility

## [1.0.3] - 2025-12-10

### Added
- Notification system with scheduling
- FCM token registration with Supabase

## [1.0.2] - 2025-12-05

### Added
- SMS filtering and categorization
- WorkManager for reliable background processing

## [1.0.1] - 2025-12-01

### Added
- Initial Supabase integration
- Basic SMS forwarding functionality

## [1.0.0] - 2025-11-25

### Added
- Initial release
- SMS receiver and forwarding
- Material 3 UI with Jetpack Compose
- Hilt dependency injection
