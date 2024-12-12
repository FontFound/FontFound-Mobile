package com.android.fontfound.ui.history

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.fontfound.data.response.DataItem

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val history by viewModel.listHistory.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            HistoryList(history = history)
        }
    }

    val errorMessage = remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            errorMessage.value = message
        }
    }

    if (errorMessage.value.isNotEmpty()) {
        Toast.makeText(LocalContext.current, errorMessage.value, Toast.LENGTH_SHORT).show()
        errorMessage.value = ""
    }
}

@Composable
fun HistoryList(
    history: List<DataItem>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            items = history,
            key = { item -> item.id ?: item.hashCode() }
        ) { historyItem ->
            HistoryCard(history = historyItem)
        }
    }
}