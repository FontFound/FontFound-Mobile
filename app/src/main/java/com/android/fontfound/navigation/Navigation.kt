package com.android.fontfound.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.android.fontfound.ui.history.HistoryScreen
import com.android.fontfound.ui.history.HistoryViewModel
import com.android.fontfound.ui.scan.ScanScreen
import com.android.fontfound.ui.settings.SettingsScreen
import com.android.fontfound.ui.settings.SettingsViewModel

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    historyViewModel : HistoryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.Scan.route,
        modifier = modifier
    ) {
        composable(TopLevelDestination.History.route) {
            HistoryScreen(viewModel = historyViewModel)
        }
        composable(TopLevelDestination.Scan.route) {
            ScanScreen()
        }
        composable(TopLevelDestination.Settings.route) {
            SettingsScreen(viewModel = settingsViewModel)
        }
    }
}