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

            if (response.status == 200) {
                val historyList = response.data?.filterNotNull() ?: emptyList()
                Result.Success(historyList)
            } else {
                Result.Error(response.message ?: "An unknown error occurred")
            }
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "An unknown error occurred")
        }
    }
}