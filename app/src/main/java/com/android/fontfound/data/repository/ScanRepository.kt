package com.android.fontfound.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.android.fontfound.data.retrofit.ApiService
import com.android.fontfound.data.util.Result
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class ScanRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun uploadHistory(imageUri: Uri, createdAt: String, updatedAt: String, result: String, deviceId: String, context: Context): Result<String> {
        return try {

            val file = uriToFile(imageUri, context)
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val createdAtBody = createdAt.toRequestBody("text/plain".toMediaTypeOrNull())
            val updatedAtBody = updatedAt.toRequestBody("text/plain".toMediaTypeOrNull())
            val resultBody = result.toRequestBody("text/plain".toMediaTypeOrNull())
            val deviceIdBody = deviceId.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.postHistory(
                file = multipartBody,
                createdAt = createdAtBody,
                updatedAt = updatedAtBody,
                result = resultBody,
                deviceId = deviceIdBody
            )

            if (response.status == 200) {
                Result.Success(response.message ?: "Upload successful")
            } else {
                Result.Error(response.message ?: "An error occurred")
            }
        } catch (e: IOException) {
            Result.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val fileName = contentResolver.getFileName(uri)
        val file = File(context.cacheDir, fileName)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return file
    }

    private fun ContentResolver.getFileName(uri: Uri): String {
        val cursor = query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && it.moveToFirst()) it.getString(nameIndex) else "temp_file.jpg"
        } ?: "temp_file.jpg"
    }
}