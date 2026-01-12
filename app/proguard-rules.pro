# Add project specific ProGuard rules here.
# For more details, see http://developer.android.com/guide/developing/tools/proguard.html

# ============================================================
# Kotlinx Serialization (CRITICAL for @Serializable classes)
# ============================================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt

# Keep Serializers
-keep,includedescriptorclasses class com.ikanisa.smsgateway.**$$serializer { *; }
-keepclassmembers class com.ikanisa.smsgateway.** {
    *** Companion;
}
-keepclasseswithmembers class com.ikanisa.smsgateway.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes and their members
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Kotlinx serialization core
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# ============================================================
# Supabase Client (CRITICAL for network operations)
# ============================================================
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# ============================================================
# Ktor (Supabase dependency - CRITICAL)
# ============================================================
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class io.ktor.client.** { *; }
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.client.plugins.** { *; }
-keep class io.ktor.serialization.** { *; }
-keep class io.ktor.utils.** { *; }

# ============================================================
# Kotlinx Coroutines
# ============================================================
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ============================================================
# OkHttp
# ============================================================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ============================================================
# Gson
# ============================================================
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================================
# WorkManager
# ============================================================
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ============================================================
# App Data Models (CRITICAL - serializable classes)
# ============================================================
-keep class com.ikanisa.smsgateway.data.model.** { *; }
-keep class com.ikanisa.smsgateway.data.repository.ActivationRepositoryImpl$AppDevice { *; }
-keep class com.ikanisa.smsgateway.data.Result { *; }
-keep class com.ikanisa.smsgateway.data.Result$Success { *; }
-keep class com.ikanisa.smsgateway.data.Result$Error { *; }

# ============================================================
# SMS Gateway App Classes
# ============================================================
-keep class com.ikanisa.smsgateway.SmsReceiver { *; }
-keep class com.ikanisa.smsgateway.ProcessSmsWorker { *; }
-keep class com.ikanisa.smsgateway.AppDefaults { *; }
-keep class com.ikanisa.smsgateway.SmsGatewayApplication { *; }
-keep class com.ikanisa.smsgateway.MainActivity { *; }
-keep class com.ikanisa.smsgateway.MainViewModel { *; }

# ============================================================
# UI Components - Enums (CRITICAL - prevent obfuscation)
# ============================================================
-keep class com.ikanisa.smsgateway.ui.components.NavDestination { *; }
-keep class com.ikanisa.smsgateway.ui.components.PulseStatus { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================================
# Jetpack Compose
# ============================================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ============================================================
# Firebase
# ============================================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ============================================================
# Hilt / Dagger (CRITICAL for dependency injection)
# ============================================================
-keep class dagger.** { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponentManager { *; }
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <methods>;
}
-keepclasseswithmembers class * {
    @dagger.* <fields>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <fields>;
}

# Keep Hilt Android components
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# ============================================================
# Preserve line numbers for crash reports
# ============================================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================
# Keep R8 from removing classes accessed via reflection
# ============================================================
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ============================================================
# Slf4j (used by Ktor/Supabase)
# ============================================================
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }