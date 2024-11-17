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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    "Dark Mode",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Change app appearance",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
            Switch(
                checked = false,
                onCheckedChange = {}
            )
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text("Language", style = MaterialTheme.typography.bodyLarge)
                Text("Change app language", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
            }
            Switch(
                checked = false,
                onCheckedChange = {}
            )
        }

        HorizontalDivider()
    }
}

@Composable
@Preview
fun SettingsScreenPreview() {
    SettingsScreen()
}