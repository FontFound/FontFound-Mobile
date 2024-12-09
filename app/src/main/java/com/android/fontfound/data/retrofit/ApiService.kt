package com.android.fontfound.data.retrofit

import com.android.fontfound.data.response.HistoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @GET("records")
    suspend fun getHistory(
    ): HistoryResponse

    @GET("records/{device-id}")
    suspend fun getHistoryByDevice(
    ): HistoryResponse

    @Multipart
    @POST("records")
    suspend fun postHistory(
        @Part file: MultipartBody.Part,
        @Part("createdAt") createdAt: RequestBody,
        @Part("updatedAt") updatedAt: RequestBody,
        @Part("result") result: RequestBody,
        @Part("device-id") deviceId: RequestBody
    ): HistoryResponse
}