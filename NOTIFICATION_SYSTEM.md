# SMS Notification System Documentation

## Overview

The SMS Gateway Android app now includes a comprehensive notification system that can send SMS notifications to payers for various purposes:

- **Daily Reminders**: Automatic daily reminders to members to continue contributions (Burimunsi Production)
- **Balance Updates**: Notifications sent after payment allocation showing updated balance
- **Burimunsi Production Notifications**: General notifications about Burimunsi production activities
- **Payment Notifications**: Notifications for payment received and allocated events

## Features

### 1. Notification Types

The system supports the following notification types (defined in `NotificationType` enum):

- `DAILY_REMINDER` - Daily reminder to members to contribute
- `BALANCE_UPDATE` - Balance notification after payment allocation
- `BURIMUNSI_PRODUCTION` - Burimunsi production related notifications
- `PAYMENT_RECEIVED` - Payment received notification
- `PAYMENT_ALLOCATED` - Payment allocated notification
- `GENERAL` - General purpose notifications

### 2. Notification Delivery Methods

#### Via Supabase Backend (Recommended)
Notifications can be sent through Supabase Edge Functions, which handle SMS sending via your configured SMS gateway provider.

#### Direct Local SMS (Device)
The app can also send SMS notifications directly from the Android device using the device's SMS capabilities.

### 3. Scheduled Notifications

#### Daily Reminders
Daily reminder notifications are automatically scheduled using WorkManager. By default, they are scheduled for 9:00 AM daily. This can be configured programmatically.

#### Balance Notifications
Balance notifications are triggered immediately when a payment is allocated to a payer.

## Usage Examples

### Using NotificationManager (Recommended)

```kotlin
@Inject
lateinit var notificationManager: NotificationManager

// Send daily reminders to all payers
notificationManager.sendDailyRemindersToAll()

// Send balance update to a specific payer
notificationManager.sendBalanceUpdateToPayer(
    payerId = "payer-123",
    balance = 15000.0,
    currency = "RWF",
    paymentAmount = 5000.0
)

// Trigger balance notification after payment allocation
notificationManager.notifyBalanceAfterPayment(
    payerId = "payer-123",
    paymentAmount = 5000.0,
    currency = "RWF"
)

// Send Burimunsi production notification
notificationManager.sendBurimunsiProductionNotification(
    message = "Important update about production",
    details = "Details here..."
)

// Configure daily reminder schedule (e.g., 8:00 AM)
notificationManager.configureDailyReminders(hour = 8, minute = 0)
```

### Using NotificationRepository

```kotlin
@Inject
lateinit var notificationRepository: NotificationRepository

// Send notification to all payers
val result = notificationRepository.sendNotificationToAll(
    type = NotificationType.DAILY_REMINDER.name,
    message = "Please continue your contributions",
    parameters = mapOf("groupName" to "Burimunsi Production")
)

// Send notification to a specific payer
val result = notificationRepository.sendNotificationToPayer(
    payerId = "payer-123",
    type = NotificationType.BALANCE_UPDATE.name,
    message = "Your balance is 15000 RWF",
    parameters = mapOf("balance" to "15000", "currency" to "RWF")
)

// Send notification to specific phone numbers
val request = NotificationRequest(
    type = NotificationType.GENERAL.name,
    recipientPhoneNumbers = listOf("+250788123456", "+250788789012"),
    message = "Custom notification message",
    parameters = emptyMap()
)
val result = notificationRepository.sendNotification(request)
```

### Using NotificationScheduler

```kotlin
@Inject
lateinit var notificationScheduler: NotificationScheduler

// Schedule daily reminders for 10:00 AM
notificationScheduler.scheduleDailyReminders(hour = 10, minute = 0)

// Cancel daily reminders
notificationScheduler.cancelDailyReminders()

// Send balance notification immediately
notificationScheduler.sendBalanceNotification(
    payerId = "payer-123",
    paymentAmount = "5000",
    currency = "RWF",
    transactionType = "payment allocation"
)
```

## Message Templates

Messages are automatically formatted using the `NotificationMessageBuilder`. Templates support Kinyarwanda and include placeholders:

### Daily Reminder Template
```
Muraho {memberName},

Mwongere gukorana na {groupName}. Twakumenyeza ko mwongere gutanga {amount} {currency}.

Murakoze!
Burimunsi Production
```

### Balance Update Template
```
Muraho {memberName},

Balance yawe ni: {balance} {currency}

Payment ya {paymentAmount} {currency} yarakoreshejwe. Balance y'ishyuye: {balance} {currency}

Murakoze!
Burimunsi Production
```

## Setup and Configuration

### 1. Dependencies

All required dependencies are already included in `build.gradle.kts`:

- Supabase Kotlin client
- Firebase Cloud Messaging
- WorkManager for scheduled tasks
- SMS Manager for direct SMS sending

### 2. Permissions

The following permissions are required (already added to `AndroidManifest.xml`):

```xml
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### 3. Supabase Configuration

#### Database Schema

Ensure your Supabase database has a `payers` table with the following structure:

```sql
CREATE TABLE payers (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  phone_number TEXT NOT NULL UNIQUE,
  name TEXT,
  balance NUMERIC(10, 2) DEFAULT 0.0,
  currency TEXT DEFAULT 'RWF',
  is_active BOOLEAN DEFAULT true,
  group_id UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

#### Optional: Notifications Log Table

For tracking sent notifications:

```sql
CREATE TABLE notifications (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  type TEXT NOT NULL,
  message TEXT NOT NULL,
  recipient_count INTEGER,
  sent_count INTEGER,
  failed_count INTEGER,
  parameters JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

#### Edge Function

Deploy the `send-notification` edge function to Supabase:

```bash
cd supabase/functions/send-notification
supabase functions deploy send-notification
```

**Important**: The edge function is configured to use MTN SMS V3 API by default. Configure your MTN credentials as described in the edge function README. Alternative providers (MessageBird, AWS SNS, Generic) are also supported.

### 4. Firebase Cloud Messaging

Firebase Cloud Messaging is configured for push notifications that can trigger SMS sending. To complete setup:

1. Add `google-services.json` to your `app/` directory
2. Configure Firebase project with Cloud Messaging enabled
3. The FCM token will be automatically registered

## WorkManager Workers

### DailyReminderWorker

- **Frequency**: Runs daily (configurable time)
- **Purpose**: Sends daily reminder notifications to all active payers
- **Scheduling**: Automatic via `NotificationScheduler`

### BalanceNotificationWorker

- **Frequency**: One-time (triggered on payment allocation)
- **Purpose**: Sends balance update notification to a specific payer
- **Triggering**: Via `NotificationScheduler.sendBalanceNotification()`

## Testing

### Test Daily Reminders

```kotlin
// In a test or debug context
val notificationScheduler = // inject or create
notificationScheduler.scheduleDailyReminders(hour = 23, minute = 59) // Schedule for near future
```

### Test Balance Notification

```kotlin
notificationScheduler.sendBalanceNotification(
    payerId = "test-payer-id",
    paymentAmount = "1000",
    currency = "RWF"
)
```

### Test Direct SMS Sending

```kotlin
val result = notificationRepository.sendLocalSms(
    phoneNumber = "+250788123456",
    message = "Test notification"
)
```

## Troubleshooting

### Daily Reminders Not Sending

1. Check WorkManager status:
   ```kotlin
   val workInfos = workManager.getWorkInfosForUniqueWork("daily_reminder_work")
   ```

2. Verify permissions: Ensure SMS sending permission is granted

3. Check logs: Look for "DailyReminderWorker" in logcat

### Balance Notifications Not Sending

1. Verify payer ID exists in database
2. Check network connectivity
3. Review notification repository logs

### SMS Sending Fails

1. Verify `SEND_SMS` permission is granted
2. Check device has SMS capability
3. For backend SMS: Verify SMS gateway configuration in Supabase function

## Best Practices

1. **Use NotificationManager**: Prefer `NotificationManager` over direct repository calls for common scenarios

2. **Error Handling**: Always check `Result` types and handle errors appropriately

3. **Rate Limiting**: Consider implementing rate limiting for bulk notifications

4. **SMS Gateway**: For production, use a dedicated SMS gateway service rather than device SMS

5. **Monitoring**: Monitor notification success/failure rates via Supabase logs

6. **Message Length**: Keep messages under 160 characters for single-part SMS, or use multipart SMS for longer messages

## Future Enhancements

- [ ] Add notification preferences per payer
- [ ] Implement notification templates customization
- [ ] Add notification delivery status tracking
- [ ] Support for scheduled notifications (send at specific time)
- [ ] Multi-language support (currently Kinyarwanda/English)
- [ ] Notification analytics and reporting
