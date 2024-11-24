package com.android.fontfound.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.android.fontfound.R

@Composable
fun LanguageOption(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(text = currentLanguage)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.language_english)) },
                onClick = {
                    expanded = false
                    onLanguageChange("English")
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.language_indonesian)) },
                onClick = {
                    expanded = false
                    onLanguageChange("Indonesian")
                }
            )
        }
    }
}