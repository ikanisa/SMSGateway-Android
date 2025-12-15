package com.example.smsgateway

import android.content.Context
import android.content.SharedPreferences
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class ProcessSmsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("SMSGatewayPrefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    companion object {
        @Volatile private var viewModel: MainViewModel? = null
        fun setViewModel(vm: MainViewModel) { viewModel = vm }
        private fun log(m: String) { viewModel?.appendLog(m) }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val sender = inputData.getString("sender") ?: "Unknown"
        val body = inputData.getString("messageBody") ?: ""
        val tsMillis = inputData.getLong("timestampMillis", System.currentTimeMillis())
        val simSlot = inputData.getInt("simSlot", -1).takeIf { it >= 0 }

        try {
            val supabaseUrl = prefs.getString("supabase_url", AppDefaults.SUPABASE_URL)?.trim().orEmpty()
            val supabaseAnon = prefs.getString("supabase_key", AppDefaults.SUPABASE_ANON_KEY)?.trim().orEmpty()
            val deviceId = prefs.getString("device_id", AppDefaults.DEVICE_ID)?.trim().orEmpty()
            val deviceSecret = prefs.getString("device_secret", AppDefaults.DEVICE_SECRET)?.trim().orEmpty()
            val deviceLabel = prefs.getString("device_label", AppDefaults.DEVICE_LABEL)?.trim().orEmpty()

            val momoMsisdn = prefs.getString("momo_msisdn", "")?.trim().orEmpty()
            val momoCode = prefs.getString("momo_code", "")?.trim().orEmpty()

            if (supabaseUrl.isEmpty() || supabaseAnon.isEmpty() || deviceId.isEmpty() || deviceSecret.isEmpty()) {
                log("❌ Not configured: open Settings and Save Supabase + Device config")
                viewModel?.incrementErrorCount()
                return@withContext Result.failure()
            }

            val url = "${supabaseUrl.removeSuffix("/")}/functions/v1/ingest-sms"

            val payload = JSONObject().apply {
                put("device_id", deviceId)
                put("device_secret", deviceSecret)
                put("sender", sender)
                put("body", body)
                put("received_at", iso8601UtcFromMillis(tsMillis))
                if (simSlot != null) put("sim_slot", simSlot)
                if (deviceLabel.isNotEmpty()) put("device_label", deviceLabel)

                val meta = JSONObject()
                if (momoMsisdn.isNotEmpty()) meta.put("momo_msisdn", momoMsisdn)
                if (momoCode.isNotEmpty()) meta.put("momo_code", momoCode)
                if (meta.length() > 0) put("meta", meta)
            }

            log("➡️ Forwarding SMS (sender=$sender)")

            val req = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", supabaseAnon)
                .addHeader("Authorization", "Bearer $supabaseAnon")
                .build()

            client.newCall(req).execute().use { resp ->
                val respText = resp.body?.string().orEmpty()

                if (resp.isSuccessful) {
                    try {
                        val json = JSONObject(respText)
                        log("✅ Saved id=${json.optString("id")} status=${json.optString("parse_status")} model=${json.optString("model_used")}")
                    } catch (_: Exception) {
                        log("✅ Saved (non-JSON response)")
                    }
                    viewModel?.incrementSmsCount()
                    return@withContext Result.success()
                }

                log("❌ Edge HTTP ${resp.code}: ${respText.take(200)}")
                viewModel?.incrementErrorCount()

                return@withContext when (resp.code) {
                    401, 403, 400, 404 -> Result.failure()
                    408, 429 -> Result.retry()
                    in 500..599 -> Result.retry()
                    else -> Result.retry()
                }
            }
        } catch (e: Exception) {
            log("❌ Exception: ${e.message}")
            viewModel?.incrementErrorCount()
            return@withContext Result.retry()
        }
    }

    private fun iso8601UtcFromMillis(ms: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(ms))
    }
}
