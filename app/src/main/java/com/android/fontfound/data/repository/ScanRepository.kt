package com.android.fontfound.data.repository

import android.widget.Toast
import com.android.fontfound.data.retrofit.ApiService
import com.android.fontfound.data.util.Result
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject

class ScanRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun uploadHistory(
        imageFile: File,
        result: String,
        deviceId: String
    ): Result<String> {
        return try {
            val imagePart = MultipartBody.Part.createFormData(
                "file",
                imageFile.name,
                imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )

            val resultPart = result.toRequestBody("text/plain".toMediaTypeOrNull())
            val deviceIdPart = deviceId.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.postHistory(
                file = imagePart,
                result = resultPart,
                deviceId = deviceIdPart
            )

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                if (responseBody.status == 201) {
                    Result.Success(responseBody.message ?: "Upload successful")
                } else {
                    Result.Error(responseBody.message ?: "Server error occurred")
                }
            } else {
                Result.Error("Failed with HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: IOException) {
            Result.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            Result.Error("Unexpected error: ${e.message}")
        }
    }
}