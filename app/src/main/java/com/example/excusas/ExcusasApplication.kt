package com.example.excusas

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.excusas.utils.PreferencesManager

class ExcusasApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Aplicar el tema guardado al iniciar la aplicación
        val preferencesManager = PreferencesManager(this)
        applyTheme(preferencesManager.getThemeMode())
    }

    private fun applyTheme(themeMode: Int) {
        when (themeMode) {
            PreferencesManager.THEME_LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            PreferencesManager.THEME_DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            PreferencesManager.THEME_SYSTEM -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
}

