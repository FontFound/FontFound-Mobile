package com.android.fontfound.ui.history

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HistoryScreen() {
    Text(
        text = "History",
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        color = Color.Black
    )
}

@Composable
@Preview
fun HistoryScreenPreview() {
    HistoryScreen()
}