package com.ikanisa.smsgateway.ui.screens

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ikanisa.smsgateway.ui.components.GradientButton
import com.ikanisa.smsgateway.ui.components.LiquidGlassCard
import com.ikanisa.smsgateway.ui.theme.BrandAccent
import com.ikanisa.smsgateway.ui.theme.BrandPrimary
import com.ikanisa.smsgateway.ui.theme.BrandSecondary
import com.ikanisa.smsgateway.ui.theme.Error
import com.ikanisa.smsgateway.ui.theme.GradientColors
import com.ikanisa.smsgateway.ui.theme.Success

/**
 * Activation screen for staff to enter MOMO code and link their device.
 */
@Composable
fun ActivationScreen(
    viewModel: ActivationViewModel = hiltViewModel(),
    onActivationComplete: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    val momoCode by viewModel.momoCode.collectAsState()
    val activationState by viewModel.activationState.collectAsState()
    val isAlreadyActivated by viewModel.isAlreadyActivated.collectAsState()
    
    // Set device ID
    LaunchedEffect(Unit) {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        viewModel.setDeviceId(deviceId ?: "unknown")
    }
    
    // Navigate on success
    LaunchedEffect(activationState) {
        if (activationState is ActivationViewModel.ActivationState.Success) {
            kotlinx.coroutines.delay(1500) // Show success animation
            onActivationComplete()
        }
    }
    
    // If already activated, navigate immediately
    LaunchedEffect(isAlreadyActivated) {
        if (isAlreadyActivated) {
            onActivationComplete()
        }
    }
    
    // Animated background
    val infiniteTransition = rememberInfiniteTransition(label = "activationBg")
    val bgOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgFloat"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Animated gradient orbs
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100 + bgOffset).dp, y = (-150 - bgOffset / 2).dp)
                .blur(150.dp)
                .alpha(0.5f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandPrimary.copy(alpha = 0.5f),
                            BrandSecondary.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = (200 - bgOffset / 2).dp, y = (500 + bgOffset).dp)
                .blur(120.dp)
                .alpha(0.4f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandAccent.copy(alpha = 0.4f),
                            Success.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // App Icon/Logo area
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.linearGradient(GradientColors.liquidGlass3D),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            Text(
                text = "Device Activation",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Enter the MOMO code provided by your administrator to activate this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // MOMO Code Input Card
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                enableShimmer = false,
                gradientColors = GradientColors.auroraGlow
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MOMO CODE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 2.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Code input field
                    OutlinedTextField(
                        value = momoCode,
                        onValueChange = { viewModel.updateMomoCode(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Enter code",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 4.sp
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                viewModel.activateDevice()
                            }
                        ),
                        isError = activationState is ActivationViewModel.ActivationState.Error,
                        enabled = activationState !is ActivationViewModel.ActivationState.Loading &&
                                activationState !is ActivationViewModel.ActivationState.Success,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            errorBorderColor = Error,
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    // Error message
                    AnimatedVisibility(
                        visible = activationState is ActivationViewModel.ActivationState.Error,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        val errorMessage = (activationState as? ActivationViewModel.ActivationState.Error)?.message ?: ""
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Activate Button / Loading / Success
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                when (activationState) {
                    is ActivationViewModel.ActivationState.Loading -> {
                        CircularProgressIndicator(
                            color = BrandPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    is ActivationViewModel.ActivationState.Success -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Activated Successfully!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Success
                            )
                        }
                    }
                    else -> {
                        GradientButton(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.activateDevice()
                            },
                            text = "Activate Device",
                            icon = Icons.Default.Verified,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = momoCode.isNotEmpty(),
                            gradient = GradientColors.primaryGradient
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Help text
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                enableShimmer = false
            ) {
                Column {
                    Text(
                        text = "Need Help?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Contact your system administrator to get your unique MOMO code. This code links your device to your account.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
