package com.ikanisa.smsgateway

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import com.ikanisa.smsgateway.ui.components.BottomNavBar
import com.ikanisa.smsgateway.ui.components.NavDestination
import com.ikanisa.smsgateway.ui.screens.HomeScreen
import com.ikanisa.smsgateway.ui.screens.SettingsScreen
import com.ikanisa.smsgateway.ui.theme.BrandAccent
import com.ikanisa.smsgateway.ui.theme.BrandPrimary
import com.ikanisa.smsgateway.ui.theme.BrandSecondary
import com.ikanisa.smsgateway.ui.theme.GradientColors
import com.ikanisa.smsgateway.ui.theme.SMSGatewayTheme
import com.ikanisa.smsgateway.ui.theme.Success

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var permissionCallback: ((Boolean) -> Unit)? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            viewModel.setListening(true)
            viewModel.appendLog("SMS permissions granted - App is listening")
        } else {
            viewModel.setListening(false)
            viewModel.appendLog("SMS permissions denied - App cannot receive SMS")
        }
        permissionCallback?.invoke(allGranted)
        permissionCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.appendLog("SMS Gateway ready")
        checkAndRequestPermissions()

        setContent {
            // Use the new Modern UI with Liquid Glass design
            com.ikanisa.smsgateway.presentation.ModernSMSGatewayApp()
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = mutableListOf<String>()

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.RECEIVE_SMS)
            }

            if (permissions.isNotEmpty()) {
                requestPermissionLauncher.launch(permissions.toTypedArray())
            } else {
                viewModel.setListening(true)
                viewModel.appendLog("SMS permissions already granted - App is listening")
            }
        } else {
            viewModel.setListening(true)
            viewModel.appendLog("App is listening (Android < 6.0)")
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasPermissions = ContextCompat.checkSelfPermission(
                this, 
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED
            viewModel.setListening(hasPermissions)
        }
    }
}

/**
 * Main app composable with animated background, screen content, and bottom navigation.
 */
@Composable
fun SMSGatewayApp(
    viewModel: MainViewModel,
    context: Context,
    logs: String,
    smsCount: Int,
    errorCount: Int,
    isListening: Boolean,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    currentDestination: NavDestination,
    onDestinationChange: (NavDestination) -> Unit
) {
    // Animated background
    val infiniteTransition = rememberInfiniteTransition(label = "bgAnim")
    val bgOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgFloat"
    )
    
    val backgroundColors = if (isDarkTheme) {
        GradientColors.darkBackground
    } else {
        GradientColors.lightBackground
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(backgroundColors)
            )
    ) {
        // Decorative gradient orbs
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = (200 + bgOffset).dp, y = (-50 - bgOffset / 2).dp)
                .blur(120.dp)
                .alpha(0.4f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandPrimary.copy(alpha = 0.4f),
                            BrandSecondary.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100 - bgOffset / 2).dp, y = (400 + bgOffset).dp)
                .blur(100.dp)
                .alpha(0.3f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandAccent.copy(alpha = 0.3f),
                            BrandPrimary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (50 + bgOffset / 3).dp, y = (700 - bgOffset / 2).dp)
                .blur(80.dp)
                .alpha(0.25f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Success.copy(alpha = 0.3f),
                            BrandAccent.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Screen content with animated transitions
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            modifier = Modifier.fillMaxSize(),
            label = "screenTransition"
        ) { destination ->
            when (destination) {
                NavDestination.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        context = context,
                        logs = logs,
                        smsCount = smsCount,
                        errorCount = errorCount,
                        isListening = isListening,
                        isDarkTheme = isDarkTheme
                    )
                }
                NavDestination.SETTINGS -> {
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle
                    )
                }
            }
        }
        
        // Bottom navigation bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            BottomNavBar(
                currentDestination = currentDestination,
                onDestinationChange = onDestinationChange,
                isDarkTheme = isDarkTheme
            )
        }
    }
}
