package com.android.fontfound.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class TopLevelDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Scan: TopLevelDestination(
        "scan",
        "Scan",
        Icons.Default.Search)
    object History: TopLevelDestination(
        "history",
        "History",
        Icons.AutoMirrored.Filled.List)
    object Settings: TopLevelDestination(
        "settings",
        "Settings",
        Icons.Default.Settings)
}