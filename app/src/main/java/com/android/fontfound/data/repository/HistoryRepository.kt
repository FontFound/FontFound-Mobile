package com.android.fontfound.data.repository

import com.android.fontfound.data.response.DataItem
import com.android.fontfound.data.retrofit.ApiService
import com.android.fontfound.data.util.Result
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun fetchHistory(): Result<List<DataItem>> {
        return try {
            val response = apiService.getHistory()
            if (response.isSuccessful) {
                val historyList = response.body()?.data?.filterNotNull() ?: emptyList()
                Result.Success(historyList)
            } else {
                Result.Error("Can't load history: ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }
}