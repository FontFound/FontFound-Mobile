package com.android.fontfound.ui.settings

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

fun updateLocale(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)

    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    context.createConfigurationContext(config)
}