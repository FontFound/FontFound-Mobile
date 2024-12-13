package com.android.fontfound.ui.history

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.fontfound.R
import com.android.fontfound.data.response.DataItem

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val history by viewModel.listHistory.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.fetchHistory() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pullRefresh(pullRefreshState)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (history.isNotEmpty()) {
            HistoryList(history = history)
        } else {
            Text(
                text = stringResource(id = R.string.no_history_message),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        if (isLoading && history.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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