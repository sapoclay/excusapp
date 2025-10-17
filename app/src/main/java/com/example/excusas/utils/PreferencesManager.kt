package com.example.excusas.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ExcusasPreferences"
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_THEME_MODE = "theme_mode"

        // Constantes para los modos de tema
        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_SYSTEM = 2

        // API Key por defecto que puedes configurar aquí
        // Si el usuario no la cambia desde la app, se usará esta
        private const val DEFAULT_API_KEY = "AIzaSyBYourAPIKeyHere"  // <-- Cambia esto por tu API key
    }

    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    fun getApiKey(): String {
        // Primero intenta obtener la API key guardada por el usuario
        val userApiKey = sharedPreferences.getString(KEY_API_KEY, null)

        // Si no hay API key guardada, usa la del código
        return if (!userApiKey.isNullOrEmpty()) {
            userApiKey
        } else {
            DEFAULT_API_KEY
        }
    }

    fun hasApiKey(): Boolean {
        val key = getApiKey()
        return key.isNotEmpty() && key != "AIzaSyBYourAPIKeyHere"
    }

    fun hasUserApiKey(): Boolean {
        // Verifica si el usuario ha guardado su propia API key
        val userApiKey = sharedPreferences.getString(KEY_API_KEY, null)
        return !userApiKey.isNullOrEmpty()
    }

    fun isUsingDefaultApiKey(): Boolean {
        // Verifica si está usando la API key del código
        return !hasUserApiKey() && DEFAULT_API_KEY != "AIzaSyBYourAPIKeyHere"
    }

    fun clearApiKey() {
        sharedPreferences.edit().remove(KEY_API_KEY).apply()
    }

    fun getDefaultApiKey(): String {
        return DEFAULT_API_KEY
    }

    // Funciones para gestionar el tema
    fun saveThemeMode(themeMode: Int) {
        sharedPreferences.edit().putInt(KEY_THEME_MODE, themeMode).apply()
    }

    fun getThemeMode(): Int {
        return sharedPreferences.getInt(KEY_THEME_MODE, THEME_SYSTEM)
    }
}
