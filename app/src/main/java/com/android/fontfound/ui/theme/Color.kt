package com.android.fontfound.ui.theme

import androidx.compose.ui.graphics.Color

sealed class ThemeColor(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val text: Color
) {
    object Dark : ThemeColor(
        background = Color(0xFF000000),
        surface = Color(0xFF000000),
        primary = Color(0xFFFFC300),
        text = Color(0xffffffff)
    )
    object Light : ThemeColor(
        background = Color(0XFFFFFFFF),
        surface = Color(0XFFFFFFFF),
        primary = Color(0xFFFFC300),
        text = Color(0xFF000000)
    )
}