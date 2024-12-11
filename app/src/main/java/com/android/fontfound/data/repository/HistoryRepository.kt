package com.android.fontfound.data.repository

import com.android.fontfound.data.response.ListEventsItem
import com.android.fontfound.data.retrofit.ApiService
import com.android.fontfound.data.util.Result
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun fetchFinishedEvents(): Result<List<ListEventsItem>> {
        return try {
            val response = apiService.getEvents(0, 40)
            if (response.isSuccessful) {
                val events = response.body()?.listEvents ?: emptyList()
                Result.Success(events)
            } else {
                Result.Error("Can't load events: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }
}