package com.ikanisa.smsgateway.ui.screens

import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ikanisa.smsgateway.data.Result
import com.ikanisa.smsgateway.data.repository.ActivationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for device activation screen.
 * Handles MOMO code validation and device activation.
 */
@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val activationRepository: ActivationRepository
) : ViewModel() {
    
    // UI State
    sealed class ActivationState {
        object Idle : ActivationState()
        object Loading : ActivationState()
        object Success : ActivationState()
        data class Error(val message: String) : ActivationState()
    }
    
    private val _activationState = MutableStateFlow<ActivationState>(ActivationState.Idle)
    val activationState: StateFlow<ActivationState> = _activationState.asStateFlow()
    
    private val _momoCode = MutableStateFlow("")
    val momoCode: StateFlow<String> = _momoCode.asStateFlow()
    
    private val _isAlreadyActivated = MutableStateFlow(false)
    val isAlreadyActivated: StateFlow<Boolean> = _isAlreadyActivated.asStateFlow()
    
    private var deviceId: String = ""
    
    init {
        checkActivationStatus()
    }
    
    fun setDeviceId(id: String) {
        deviceId = id
    }
    
    fun updateMomoCode(code: String) {
        _momoCode.value = code.uppercase().filter { it.isLetterOrDigit() }
        // Reset error state when user types
        if (_activationState.value is ActivationState.Error) {
            _activationState.value = ActivationState.Idle
        }
    }
    
    fun checkActivationStatus() {
        viewModelScope.launch {
            val isActivated = activationRepository.isDeviceActivated()
            _isAlreadyActivated.value = isActivated
            
            if (isActivated) {
                val storedCode = activationRepository.getStoredMomoCode()
                _momoCode.value = storedCode ?: ""
            }
        }
    }
    
    fun activateDevice() {
        val code = _momoCode.value.trim()
        
        // Validate input
        if (code.isEmpty()) {
            _activationState.value = ActivationState.Error("Please enter your MOMO code")
            return
        }
        
        if (code.length < 4) {
            _activationState.value = ActivationState.Error("MOMO code must be at least 4 characters")
            return
        }
        
        viewModelScope.launch {
            _activationState.value = ActivationState.Loading
            
            // First validate the MOMO code
            when (val validationResult = activationRepository.validateMomoCode(code)) {
                is Result.Success -> {
                    if (validationResult.data) {
                        // MOMO code is valid, proceed with activation
                        when (val activationResult = activationRepository.activateDevice(code, deviceId)) {
                            is Result.Success -> {
                                _isAlreadyActivated.value = true
                                _activationState.value = ActivationState.Success
                            }
                            is Result.Error -> {
                                _activationState.value = ActivationState.Error(
                                    activationResult.message ?: "Activation failed. Please try again."
                                )
                            }
                        }
                    } else {
                        _activationState.value = ActivationState.Error(
                            "Invalid MOMO code. Please contact your administrator."
                        )
                    }
                }
                is Result.Error -> {
                    _activationState.value = ActivationState.Error(
                        validationResult.message ?: "Validation failed. Please check your connection."
                    )
                }
            }
        }
    }
    
    fun resetState() {
        _activationState.value = ActivationState.Idle
    }
}
