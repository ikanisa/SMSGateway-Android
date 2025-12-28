package com.example.smsgateway

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.ikanisa.smsgateway.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private lateinit var tvGatewayState: TextView
    private lateinit var tvMomoState: TextView
    private lateinit var btnEditGateway: MaterialButton

    private lateinit var btnGrantSms: MaterialButton
    private lateinit var btnOpenNotifications: MaterialButton
    private lateinit var btnBattery: MaterialButton

    companion object {
        private const val PREFS_NAME = "SMSGatewayPrefs"
        private const val KEY_SUPABASE_URL = "supabase_url"
        private const val KEY_SUPABASE_KEY = "supabase_key"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DEVICE_SECRET = "device_secret"
        private const val KEY_DEVICE_LABEL = "device_label"
        private const val KEY_MOMO_MSISDN = "momo_msisdn"
        private const val KEY_MOMO_CODE = "momo_code"
    }

    private val smsPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        updatePermissionUi()
        Toast.makeText(this, "Permissions updated", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        findViewById<MaterialToolbar>(R.id.toolbarSettings).apply {
            setNavigationOnClickListener { finish() }
        }

        tvGatewayState = findViewById(R.id.tvGatewayState)
        tvMomoState = findViewById(R.id.tvMomoState)
        btnEditGateway = findViewById(R.id.btnEditGateway)

        btnGrantSms = findViewById(R.id.btnGrantSms)
        btnOpenNotifications = findViewById(R.id.btnOpenNotifications)
        btnBattery = findViewById(R.id.btnBattery)

        btnEditGateway.setOnClickListener { openGatewayDialog() }
        btnGrantSms.setOnClickListener { requestSmsPermissions() }
        btnOpenNotifications.setOnClickListener { openNotificationSettings() }
        btnBattery.setOnClickListener { requestBatteryOptimization() }

        refreshSummary()
        updatePermissionUi()
    }

    override fun onResume() {
        super.onResume()
        refreshSummary()
        updatePermissionUi()
    }

    private fun refreshSummary() {
        val supaUrl = prefs.getString(KEY_SUPABASE_URL, "").orEmpty().trim()
        val supaKey = prefs.getString(KEY_SUPABASE_KEY, "").orEmpty().trim()
        val devId = prefs.getString(KEY_DEVICE_ID, "").orEmpty().trim()
        val devSecret = prefs.getString(KEY_DEVICE_SECRET, "").orEmpty().trim()

        val configured = supaUrl.isNotEmpty() && supaKey.isNotEmpty() && devId.isNotEmpty() && devSecret.isNotEmpty()

        tvGatewayState.text = if (configured) {
            "Configured ✅"
        } else {
            "Not configured — tap “Edit credentials”"
        }

        val momo = prefs.getString(KEY_MOMO_MSISDN, "").orEmpty().trim()
        val momoCode = prefs.getString(KEY_MOMO_CODE, "").orEmpty().trim()

        tvMomoState.text = when {
            momo.isNotEmpty() && momoCode.isNotEmpty() -> "MoMo: $momo (code: $momoCode)"
            momo.isNotEmpty() -> "MoMo: $momo"
            momoCode.isNotEmpty() -> "MoMo code: $momoCode"
            else -> "Not set"
        }
    }

    private fun openGatewayDialog() {
        val v = layoutInflater.inflate(R.layout.dialog_gateway_config, null)

        val etSupabaseUrl = v.findViewById<TextInputEditText>(R.id.etSupabaseUrl)
        val etSupabaseKey = v.findViewById<TextInputEditText>(R.id.etSupabaseKey)
        val etDeviceId = v.findViewById<TextInputEditText>(R.id.etDeviceId)
        val etDeviceSecret = v.findViewById<TextInputEditText>(R.id.etDeviceSecret)
        val etDeviceLabel = v.findViewById<TextInputEditText>(R.id.etDeviceLabel)
        val etMomoNumber = v.findViewById<TextInputEditText>(R.id.etMomoNumber)
        val etMomoCode = v.findViewById<TextInputEditText>(R.id.etMomoCode)

        // Prefill
        etSupabaseUrl.setText(prefs.getString(KEY_SUPABASE_URL, ""))
        etSupabaseKey.setText(prefs.getString(KEY_SUPABASE_KEY, ""))
        etDeviceId.setText(prefs.getString(KEY_DEVICE_ID, ""))
        etDeviceSecret.setText(prefs.getString(KEY_DEVICE_SECRET, ""))
        etDeviceLabel.setText(prefs.getString(KEY_DEVICE_LABEL, ""))

        etMomoNumber.setText(prefs.getString(KEY_MOMO_MSISDN, ""))
        etMomoCode.setText(prefs.getString(KEY_MOMO_CODE, ""))

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Gateway credentials")
            .setView(v)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val supaUrl = etSupabaseUrl.text?.toString()?.trim().orEmpty()
                val supaKey = etSupabaseKey.text?.toString()?.trim().orEmpty()
                val devId = etDeviceId.text?.toString()?.trim().orEmpty()
                val devSecret = etDeviceSecret.text?.toString()?.trim().orEmpty()
                val devLabel = etDeviceLabel.text?.toString()?.trim().orEmpty()
                val momo = etMomoNumber.text?.toString()?.trim().orEmpty()
                val momoCode = etMomoCode.text?.toString()?.trim().orEmpty()

                if (supaUrl.isEmpty() || supaKey.isEmpty() || devId.isEmpty() || devSecret.isEmpty()) {
                    Toast.makeText(this, "Supabase URL, Anon Key, Device ID, and Device Secret are required.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                prefs.edit()
                    .putString(KEY_SUPABASE_URL, supaUrl)
                    .putString(KEY_SUPABASE_KEY, supaKey)
                    .putString(KEY_DEVICE_ID, devId)
                    .putString(KEY_DEVICE_SECRET, devSecret)
                    .putString(KEY_DEVICE_LABEL, devLabel)
                    .putString(KEY_MOMO_MSISDN, momo)
                    .putString(KEY_MOMO_CODE, momoCode)
                    .apply()

                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                refreshSummary()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun hasSmsPerms(): Boolean {
        val rcv = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        return rcv && read
    }

    private fun updatePermissionUi() {
        val ok = hasSmsPerms()
        btnGrantSms.isEnabled = !ok
        btnGrantSms.text = if (ok) "SMS permissions granted" else "Grant SMS permissions"
    }

    private fun requestSmsPermissions() {
        smsPermLauncher.launch(arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS))
    }

    private fun openNotificationSettings() {
        try {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }

    private fun requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            try {
                startActivity(intent)
            } catch (_: Exception) {
                val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(settingsIntent)
            }
        } else {
            Toast.makeText(this, "Battery optimization settings not available on this Android version", Toast.LENGTH_SHORT).show()
        }
    }
}
