package com.example.excusas.api

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    @SerializedName("contents")
    val contents: List<Content>,
    @SerializedName("generationConfig")
    val generationConfig: GenerationConfig = GenerationConfig()
)

data class Content(
    @SerializedName("parts")
    val parts: List<Part>?,
    @SerializedName("role")
    val role: String? = null
)

data class Part(
    @SerializedName("text")
    val text: String
)

data class GenerationConfig(
    @SerializedName("temperature")
    val temperature: Double = 1.2,  // Reducido un poco para más control
    @SerializedName("maxOutputTokens")
    val maxOutputTokens: Int = 300,  // Aumentado para soportar thinking tokens + respuesta
    @SerializedName("topP")
    val topP: Double = 0.95,
    @SerializedName("topK")
    val topK: Int = 40
)

data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<Candidate>
)

data class Candidate(
    @SerializedName("content")
    val content: Content
)
