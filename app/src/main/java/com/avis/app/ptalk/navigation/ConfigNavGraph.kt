package com.avis.app.ptalk.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.avis.app.ptalk.ui.screen.auth.LoginScreen
import com.avis.app.ptalk.ui.screen.auth.SignupScreen
import com.avis.app.ptalk.ui.screen.config.HomeScreen
import com.avis.app.ptalk.ui.screen.config.ScanDeviceScreen

/**
 * Navigation for PTalk app including Auth and Config
 */
@Composable
fun ConfigAppNavGraph(
    navController: NavHostController,
    startDestination: String = Route.LOGIN,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Route.LOGIN) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Route.HOME) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Route.SIGNUP)
                }
            )
        }
        
        composable(Route.SIGNUP) {
            SignupScreen(
                onNavigateToHome = {
                    navController.navigate(Route.HOME) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Home screen with PTIT logo
        composable(Route.HOME) {
            HomeScreen(
                onNavigateToScan = {
                    navController.navigate(Route.SCAN_DEVICE)
                },
                onNavigateToControl = { macAddress, deviceName ->
                    navController.navigate("\${Route.CONTROL}/\$macAddress/\$deviceName")
                }
            )
        }
        
        // Scan device screen with radar
        composable(Route.SCAN_DEVICE) {
            ScanDeviceScreen(
                onDeviceConnected = { deviceAddress ->
                    // Device connected, config dialog will show
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("\${Route.CONTROL}/{macAddress}/{deviceName}") { backStackEntry ->
            val macAddress = backStackEntry.arguments?.getString("macAddress") ?: ""
            val deviceName = backStackEntry.arguments?.getString("deviceName") ?: "PTalk Device"
            com.avis.app.ptalk.ui.screen.config.ControlScreen(
                macAddress = macAddress,
                deviceName = deviceName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
