package com.example.excusas.repository

import android.content.Context
import com.example.excusas.api.Content
import com.example.excusas.api.GeminiRequest
import com.example.excusas.api.Part
import com.example.excusas.api.RetrofitClient
import com.example.excusas.utils.PreferencesManager

class ExcuseRepository(context: Context) {

    private val preferencesManager = PreferencesManager(context)
    private var apiKey: String = preferencesManager.getApiKey()

    fun updateApiKey(newKey: String) {
        apiKey = newKey
    }

    fun isApiKeyConfigured(): Boolean {
        return preferencesManager.hasApiKey()
    }

    suspend fun generateExcuseWithAI(category: String): Result<String> {
        // Obtener la API key más reciente de las preferencias
        apiKey = preferencesManager.getApiKey()

        // Verificar si la API key está configurada
        if (!isApiKeyConfigured()) {
            return Result.failure(Exception("API_KEY_NOT_CONFIGURED"))
        }

        return try {
            val prompt = buildPrompt(category)
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt)
                        )
                    )
                )
            )

            val response = RetrofitClient.geminiApi.generateContent(
                apiKey = apiKey,
                request = request
            )

            if (response.isSuccessful) {
                val responseBody = response.body()

                // Log detallado para depuración
                android.util.Log.d("ExcuseRepository", "=== RESPUESTA DE GEMINI API ===")
                android.util.Log.d("ExcuseRepository", "Response completo: $responseBody")
                android.util.Log.d("ExcuseRepository", "Candidates: ${responseBody?.candidates}")
                android.util.Log.d("ExcuseRepository", "Número de candidates: ${responseBody?.candidates?.size}")

                val candidate = responseBody?.candidates?.firstOrNull()
                android.util.Log.d("ExcuseRepository", "Primer candidate: $candidate")

                val contentPart = candidate?.content
                android.util.Log.d("ExcuseRepository", "Content: $contentPart")
                android.util.Log.d("ExcuseRepository", "Parts: ${contentPart?.parts}")

                val textPart = contentPart?.parts?.firstOrNull()
                android.util.Log.d("ExcuseRepository", "Primer part: $textPart")

                val excuse = textPart?.text?.trim()
                android.util.Log.d("ExcuseRepository", "Texto extraído: '$excuse'")
                android.util.Log.d("ExcuseRepository", "================================")

                if (!excuse.isNullOrEmpty()) {
                    Result.success(excuse)
                } else {
                    android.util.Log.e("ExcuseRepository", "ERROR: La respuesta no contiene texto generado")
                    android.util.Log.e("ExcuseRepository", "Razón: parts es null o vacío")
                    Result.failure(Exception("NO_EXCUSE_GENERATED"))
                }
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string() ?: "Sin detalles del error"
                android.util.Log.e("ExcuseRepository", "Error API: Code=$errorCode, Body=$errorBody")
                Result.failure(Exception("API_ERROR_$errorCode: $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ExcuseRepository", "Excepción al llamar API", e)
            Result.failure(e)
        }
    }

    private fun buildPrompt(category: String): String {
        return when (category) {
            "Trabajo" -> """
                Responde SOLO en español. Genera una única excusa breve (máximo 45 palabras) para llegar tarde al trabajo que sea imaginativa.
                Formato: Solo la excusa, sin introducciones, sin explicaciones adicionales, sin comillas.
                Ejemplo: "El metro se retrasó por una incidencia técnica."
            """.trimIndent()

            "Estudio" -> """
                Responde SOLO en español. Genera una única excusa breve (máximo 45 palabras) para no entregar una tarea o no asistir a clase. La explicación debe ser imaginativa.
                Formato: Solo la excusa, sin introducciones, sin explicaciones adicionales, sin comillas.
                Ejemplo: "Mi ordenador se reinició y perdí todo el trabajo."
            """.trimIndent()

            "Familia" -> """
                Responde SOLO en español. Genera una única excusa breve (máximo 45 palabras) para no ir a un evento familiar. La excusa debe ser imaginativa y divertida.
                Formato: Solo la excusa, sin introducciones, sin explicaciones adicionales, sin comillas.
                Ejemplo: "Tengo un compromiso de trabajo que no puedo cancelar."
            """.trimIndent()

            "Amigos" -> """
                Responde SOLO en español. Genera una única excusa breve (máximo 45 palabras) para cancelar planes con amigos. La excusa debe ser imaginativa y divertida.
                Formato: Solo la excusa, sin introducciones, sin explicaciones adicionales, sin comillas.
                Ejemplo: "Me surgió una emergencia familiar de último momento."
            """.trimIndent()

            "Citas" -> """
                Responde SOLO en español. Genera una única excusa breve (máximo 45 palabras) para cancelar una cita. La excusa debe ser imaginativa y divertida.
                Formato: Solo la excusa, sin introducciones, sin explicaciones adicionales, sin comillas.
                Ejemplo: "No me siento bien, creo que comí algo en mal estado."
            """.trimIndent()

            else -> """
                Responde SOLO en español. Genera una única excusa breve (máximo 45 palabras) para cualquier situación y que sea imaginativa y divertida.
                Formato: Solo la excusa, sin introducciones, sin explicaciones adicionales, sin comillas.
                Ejemplo: "Tengo un problema urgente que debo resolver ahora mismo."
            """.trimIndent()
        }
    }
}
