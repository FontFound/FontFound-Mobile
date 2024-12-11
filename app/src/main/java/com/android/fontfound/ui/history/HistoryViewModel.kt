package com.android.fontfound.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.fontfound.data.repository.HistoryRepository
import com.android.fontfound.data.response.ListEventsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.android.fontfound.data.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _listEvents = MutableLiveData<List<ListEventsItem>>(emptyList())
    val listEvents: LiveData<List<ListEventsItem>> = _listEvents

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        fetchFinishedEvents()
    }

    private fun fetchFinishedEvents() = viewModelScope.launch {
        _isLoading.value = true
        val result = historyRepository.fetchFinishedEvents()
        _isLoading.value = false
        when (result) {
            is Result.Success -> {
                _listEvents.value = result.data
            }

            is Result.Error -> {
                _errorMessage.emit("Failed to load event data")
            }
        }
    }
}