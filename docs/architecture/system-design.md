# System Architecture

## Overview

SMS Gateway Android is a production-ready mobile application that forwards SMS messages to a Supabase backend for centralized processing and storage.

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    SMS Gateway Android                       │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────────┐ │
│  │   UI Layer  │  │  ViewModel   │  │   Presentation      │ │
│  │   (Compose) │◄─┤   Layer      │◄─┤   Navigation        │ │
│  └─────────────┘  └──────────────┘  └─────────────────────┘ │
│           │                                                  │
│           ▼                                                  │
│  ┌─────────────────────────────────────────────────────────┐│
│  │               Repository Layer                          ││
│  │  ┌────────────┐ ┌────────────┐ ┌────────────────────┐  ││
│  │  │ SmsRepo    │ │ Activation │ │ NotificationRepo   │  ││
│  │  │            │ │ Repo       │ │                    │  ││
│  │  └────────────┘ └────────────┘ └────────────────────┘  ││
│  └─────────────────────────────────────────────────────────┘│
│           │                                                  │
│           ▼                                                  │
│  ┌─────────────────────────────────────────────────────────┐│
│  │               Data Sources                              ││
│  │  ┌────────────┐ ┌────────────┐ ┌────────────────────┐  ││
│  │  │ Supabase   │ │ WorkManager│ │ Firebase Cloud     │  ││
│  │  │ Postgrest  │ │            │ │ Messaging          │  ││
│  │  └────────────┘ └────────────┘ └────────────────────┘  ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
              ┌────────────────────────┐
              │     Supabase Backend   │
              │  ┌──────────────────┐  │
              │  │   sms_messages   │  │
              │  │   app_devices    │  │
              │  │   notifications  │  │
              │  └──────────────────┘  │
              └────────────────────────┘
```

## Component Responsibilities

### Presentation Layer
- **MainActivity**: Entry point with `@AndroidEntryPoint` for Hilt
- **MainViewModel**: State management and UI logic
- **Compose Screens**: Material 3 UI with glassmorphism effects

### Infrastructure Layer
- **SmsReceiver**: BroadcastReceiver for incoming SMS
- **ProcessSmsWorker**: WorkManager for reliable background processing
- **MyFirebaseMessagingService**: FCM token handling

### Data Layer
- **Repositories**: Abstract data access patterns
- **Datasources**: Supabase API clients
- **Models**: Data transfer objects

### Dependency Injection
- Hilt modules for singleton dependencies
- WorkManager integration with `HiltWorkerFactory`

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt 2.48 |
| Networking | Supabase Kotlin SDK |
| Background | WorkManager |
| Push | Firebase Cloud Messaging |
| Analytics | Firebase Analytics + Crashlytics |
