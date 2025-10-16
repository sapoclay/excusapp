package com.example.excusas.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    // Endpoint v1 con gemini-2.0-flash-001 (sin thinking tokens)
    @POST("v1/models/gemini-2.0-flash-001:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>

}
