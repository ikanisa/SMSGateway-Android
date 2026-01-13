package com.ikanisa.smsgateway.util

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extension functions for common operations.
 */

// String extensions

/**
 * Returns true if the string is a valid phone number.
 */
fun String.isValidPhoneNumber(): Boolean {
    return this.matches(Regex("^[+]?[0-9]{10,15}$"))
}

/**
 * Masks a phone number for display, showing only first 3 and last 2 digits.
 */
fun String.maskPhoneNumber(): String {
    if (this.length < 6) return this
    return "${this.take(3)}****${this.takeLast(2)}"
}

/**
 * Truncates string to specified length with ellipsis.
 */
fun String.truncate(maxLength: Int): String {
    return if (this.length > maxLength) {
        "${this.take(maxLength - 3)}..."
    } else {
        this
    }
}

// Context extensions

/**
 * Shows a short toast message.
 */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Shows a long toast message.
 */
fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// Date extensions

/**
 * Formats a Date to readable string.
 */
fun Date.toReadableString(pattern: String = "MMM dd, yyyy HH:mm"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}

/**
 * Formats a timestamp to readable string.
 */
fun Long.toReadableDate(pattern: String = "MMM dd, yyyy HH:mm"): String {
    return Date(this).toReadableString(pattern)
}

// Coroutine extensions

/**
 * Launch a coroutine on the main thread.
 */
fun CoroutineScope.launchOnMain(block: suspend CoroutineScope.() -> Unit) {
    launch(Dispatchers.Main) { block() }
}

/**
 * Launch a coroutine on the IO thread.
 */
fun CoroutineScope.launchOnIO(block: suspend CoroutineScope.() -> Unit) {
    launch(Dispatchers.IO) { block() }
}

// Result extensions

/**
 * Maps a successful Result to a new value.
 */
inline fun <T, R> com.ikanisa.smsgateway.data.Result<T>.mapSuccess(
    transform: (T) -> R
): com.ikanisa.smsgateway.data.Result<R> {
    return when (this) {
        is com.ikanisa.smsgateway.data.Result.Success -> 
            com.ikanisa.smsgateway.data.Result.Success(transform(data))
        is com.ikanisa.smsgateway.data.Result.Error -> 
            com.ikanisa.smsgateway.data.Result.Error(message)
    }
}

/**
 * Returns the data or null for error results.
 */
fun <T> com.ikanisa.smsgateway.data.Result<T>.getOrNull(): T? {
    return when (this) {
        is com.ikanisa.smsgateway.data.Result.Success -> data
        is com.ikanisa.smsgateway.data.Result.Error -> null
    }
}
