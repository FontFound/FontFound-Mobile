package com.android.fontfound.data.retrofit

import com.android.fontfound.data.response.HistoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @GET("records/{device-id}")
    suspend fun getHistoryByDevice(
        @Path("device-id") deviceId: String
    ): Response<HistoryResponse>

    @Multipart
    @POST("records")
    suspend fun postHistory(
        @Part file: MultipartBody.Part,
        @Part("result") result: RequestBody,
        @Part("device_id") deviceId: RequestBody
    ): Response<HistoryResponse>
}