package com.android.fontfound.data.retrofit

import com.android.fontfound.data.response.EventResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("events")
    suspend fun getEvents(
        @Query("active") active: Int,
        @Query("limit") limit: Int,
    ): Response<EventResponse>
}