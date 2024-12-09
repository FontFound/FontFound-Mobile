package com.android.fontfound.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.fontfound.R

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val isDarkMode = viewModel.getThemeSettings().observeAsState(initial = false)
    val currentLanguage = viewModel.getLanguageSetting().observeAsState(initial = "English")
    val context = LocalContext.current

    var selectedLanguage by remember { mutableStateOf(currentLanguage.value) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.dark_mode), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.dark_mode_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
            Switch(
                checked = isDarkMode.value,
                onCheckedChange = { viewModel.saveThemeSetting(it) }
            )
        }
        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.language), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.language_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
            LanguageOption(
                currentLanguage = selectedLanguage,
                onLanguageChange = { newLanguage ->
                    if (newLanguage != selectedLanguage) {
                        selectedLanguage = newLanguage
                        viewModel.saveLanguageSetting(newLanguage)

                        val languageCode = when (newLanguage) {
                            "English" -> "en"
                            "Indonesian" -> "id"
                            else -> "en"
                        }
                        updateLocale(context, languageCode)
                    }
                }
            )
        }
        HorizontalDivider()
    }
}