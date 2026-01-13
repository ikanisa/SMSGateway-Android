package com.ikanisa.smsgateway.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setAutoSync(enabled: Boolean) {
        _uiState.update { it.copy(autoSync = enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        _uiState.update { it.copy(notifications = enabled) }
    }

    fun setHapticFeedback(enabled: Boolean) {
        _uiState.update { it.copy(hapticFeedback = enabled) }
    }

    fun setBiometricLock(enabled: Boolean) {
        _uiState.update { it.copy(biometricLock = enabled) }
    }
}

data class SettingsUiState(
    val autoSync: Boolean = true,
    val notifications: Boolean = true,
    val hapticFeedback: Boolean = true,
    val biometricLock: Boolean = false,
    val appVersion: String = "1.0.0",
    val momoCode: String = "Not Set"
)
