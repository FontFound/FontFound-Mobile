package com.android.fontfound

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.android.fontfound.preferences.SettingsPreferences
import com.android.fontfound.preferences.dataStore
import com.android.fontfound.ui.MainScreen
import com.android.fontfound.ui.history.HistoryViewModel
import com.android.fontfound.ui.scan.ScanViewModel
import com.android.fontfound.ui.settings.SettingsViewModel
import com.android.fontfound.ui.settings.updateLocale
import com.android.fontfound.ui.theme.Theme
import dagger.hilt.android.AndroidEntryPoint

import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsPreferences = SettingsPreferences.getInstance(dataStore)

        val isDarkMode = settingsPreferences.getInitialThemeSetting()
        val initialLanguage = settingsPreferences.getInitialLanguageSetting()
        val languageCode = when (initialLanguage) {
            "Indonesian" -> "id"
            else -> "en"
        }

        updateLocale(this, languageCode)

        val settingsViewModel: SettingsViewModel by viewModels()

        settingsViewModel.getLanguageSetting().observe(this) { language ->
            val updatedLanguageCode = when (language) {
                "Indonesian" -> "id"
                else -> "en"
            }
            if (Locale.getDefault().language != updatedLanguageCode) {
                updateLocale(this, updatedLanguageCode)
            }
        }

        setContent {
            val isDarkTheme = settingsViewModel.getThemeSettings().observeAsState(initial = isDarkMode)
            val historyViewModel: HistoryViewModel by viewModels()
            val scanViewModel: ScanViewModel by viewModels()

            Theme(isDarkTheme = isDarkTheme.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        settingsViewModel = settingsViewModel,
                        historyViewModel = historyViewModel,
                        scanViewModel = scanViewModel
                    )
                }
            }
        }
    }
}