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
import com.android.fontfound.data.response.ListEventsItem

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val events by viewModel.listEvents.observeAsState(emptyList())
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
            EventList(events = events)
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
fun EventList(
    events: List<ListEventsItem>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            items = events,
            key = { event -> event.id }
        ) { event ->
            HistoryCard(event = event)
        }
    }
}