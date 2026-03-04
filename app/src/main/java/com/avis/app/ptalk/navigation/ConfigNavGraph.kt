package com.avis.app.ptalk.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.avis.app.ptalk.ui.screen.config.HomeScreen
import com.avis.app.ptalk.ui.screen.config.ScanDeviceScreen

/**
 * Simplified navigation for config-only app
 * Home -> Scan -> Config
 */
@Composable
fun ConfigAppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.HOME,
        modifier = modifier
    ) {
        // Home screen with PTIT logo
        composable(Route.HOME) {
            HomeScreen(
                onNavigateToScan = {
                    navController.navigate(Route.SCAN_DEVICE)
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
    }
}
