package com.android.fontfound.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


private val DarkModeScheme = darkColorScheme(
    primary = ThemeColor.Dark.primary,
    onPrimary = ThemeColor.Dark.text,
    surface = ThemeColor.Dark.surface,
    background = ThemeColor.Dark.background
)

private val LightModeScheme = lightColorScheme(
    primary = ThemeColor.Light.primary,
    onPrimary = ThemeColor.Light.text,
    surface = ThemeColor.Light.surface,
    background = ThemeColor.Light.background
)

@Composable
fun Theme(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) DarkModeScheme else LightModeScheme,
        typography = Typography,
        content = content
    )
}