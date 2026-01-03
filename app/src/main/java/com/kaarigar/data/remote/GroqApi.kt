package com.kaarigar.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApi {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(
            @Header("Authorization") authorization: String,
            @Body request: GroqRequest
    ): Response<GroqResponse>
}
