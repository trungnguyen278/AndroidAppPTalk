package com.avis.app.ptalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.avis.app.ptalk.navigation.ConfigAppNavGraph
import com.avis.app.ptalk.ui.theme.AppColors
import com.avis.app.ptalk.ui.theme.appColors
import dagger.hilt.android.AndroidEntryPoint
import org.thingai.android.module.meo.MeoSdk
import org.thingai.base.log.ILog

// Global composition local for theme colors
val LocalAppColors = compositionLocalOf<AppColors> { error("No AppColors provided") }

/**
 * Simplified MainActivity for config-only app
 * No authentication, no database, just BLE config
 * Supports system dark/light theme
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ILog.logLevel = ILog.DEBUG
        ILog.ENABLE_LOGGING = true

        MeoSdk.init(this.applicationContext)

        // Simple splash screen - no auth check needed
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current
            
            // Use system theme
            val colors = appColors(isSystemInDarkTheme())

            CompositionLocalProvider(LocalAppColors provides colors) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        },
                    color = colors.background
                ) {
                    ConfigAppNavGraph(
                        navController = navController,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}