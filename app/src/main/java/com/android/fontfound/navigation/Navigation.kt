package com.android.fontfound.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.android.fontfound.ui.history.HistoryScreen
import com.android.fontfound.ui.scan.ScanScreen
import com.android.fontfound.ui.settings.SettingsScreen

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onShowBottomBar: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.Scan.route,
        modifier = modifier
    ) {
        composable(TopLevelDestination.History.route) {
            onShowBottomBar(true)
            HistoryScreen()
        }
        composable(TopLevelDestination.Scan.route) {
            onShowBottomBar(false)
            ScanScreen(navController)
        }
        composable(TopLevelDestination.Settings.route) {
            onShowBottomBar(true)
            SettingsScreen()
        }
    }
}