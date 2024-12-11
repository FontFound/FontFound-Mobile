package com.android.fontfound.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.fontfound.R

sealed class TopLevelDestination(
    val route: String,
    val icon: ImageVector?,
    @DrawableRes val iconResId: Int?
) {
    object Scan: TopLevelDestination(
        "scan",
        null,
        R.drawable.ic_camera)
    object History: TopLevelDestination(
        "history",
        null,
        R.drawable.ic_history)
    object Settings: TopLevelDestination(
        "settings",
        Icons.Default.Settings,
        null)
}