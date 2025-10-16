package com.example.excusas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.excusas.databinding.ActivitySettingsBinding
import com.example.excusas.repository.ExcuseRepository
import com.example.excusas.utils.PreferencesManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var repository: ExcuseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)
        repository = ExcuseRepository(this)

        setupUI()
        setupListeners()
        loadSavedApiKey()
        updateStatusText()
    }

    private fun setupUI() {
        // Configurar ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            saveApiKey()
        }

        binding.btnTest.setOnClickListener {
            testApiKey()
        }

        binding.btnGetApiKey.setOnClickListener {
            openGeminiWebsite()
        }

        binding.btnRestoreDefault.setOnClickListener {
            restoreDefaultApiKey()
        }
    }

    private fun loadSavedApiKey() {
        val savedKey = preferencesManager.getApiKey()
        if (savedKey.isNotEmpty() && savedKey != "AIzaSyBYourAPIKeyHere") {
            binding.etApiKey.setText(savedKey)
        }
    }

    private fun updateStatusText() {
        val statusText = when {
            preferencesManager.hasUserApiKey() -> getString(R.string.using_user_key)
            preferencesManager.isUsingDefaultApiKey() -> getString(R.string.using_default_key)
            else -> ""
        }
        binding.tvStatus.text = statusText
    }

    private fun saveApiKey() {
        val apiKey = binding.etApiKey.text.toString().trim()

        if (apiKey.isEmpty()) {
            Toast.makeText(this, R.string.api_key_empty, Toast.LENGTH_SHORT).show()
            return
        }

        preferencesManager.saveApiKey(apiKey)
        Toast.makeText(this, R.string.api_key_saved, Toast.LENGTH_SHORT).show()

        // Actualizar el repositorio con la nueva key
        repository.updateApiKey(apiKey)
        updateStatusText()
    }

    private fun restoreDefaultApiKey() {
        val defaultKey = preferencesManager.getDefaultApiKey()

        if (defaultKey == "AIzaSyBYourAPIKeyHere") {
            Toast.makeText(
                this,
                "No hay API key por defecto configurada en el código",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Limpiar la API key personalizada del usuario
        preferencesManager.clearApiKey()

        // Mostrar la API key por defecto en el campo
        binding.etApiKey.setText(defaultKey)

        Toast.makeText(this, R.string.api_key_restored, Toast.LENGTH_SHORT).show()
        updateStatusText()
    }

    private fun testApiKey() {
        val apiKey = binding.etApiKey.text.toString().trim()

        if (apiKey.isEmpty()) {
            Toast.makeText(this, R.string.api_key_empty, Toast.LENGTH_SHORT).show()
            return
        }

        // Deshabilitar botón mientras se prueba
        binding.btnTest.isEnabled = false
        binding.btnTest.text = getString(R.string.api_key_testing)

        lifecycleScope.launch {
            try {
                // Crear un request directamente para probar
                val request = com.example.excusas.api.GeminiRequest(
                    contents = listOf(
                        com.example.excusas.api.Content(
                            parts = listOf(
                                com.example.excusas.api.Part(
                                    text = "Di solo: Hola"
                                )
                            )
                        )
                    )
                )

                val response = com.example.excusas.api.RetrofitClient.geminiApi.generateContent(
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()

                    // Log detallado para depuración
                    android.util.Log.d("SettingsActivity", "Response completo: $responseBody")
                    android.util.Log.d("SettingsActivity", "Candidates: ${responseBody?.candidates}")
                    android.util.Log.d("SettingsActivity", "Candidates size: ${responseBody?.candidates?.size}")

                    val text = responseBody?.candidates?.firstOrNull()
                        ?.content?.parts?.firstOrNull()?.text

                    android.util.Log.d("SettingsActivity", "Texto extraído: $text")

                    if (!text.isNullOrEmpty()) {
                        Toast.makeText(
                            this@SettingsActivity,
                            R.string.api_key_valid,
                            Toast.LENGTH_LONG
                        ).show()
                        // Si funciona, guardar automáticamente
                        preferencesManager.saveApiKey(apiKey)
                        updateStatusText()
                    } else {
                        Toast.makeText(
                            this@SettingsActivity,
                            "La API respondió pero no generó texto. Revisa los logs.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                    android.util.Log.e("SettingsActivity", "Error API: Code=$errorCode, Body=$errorBody")

                    val message = when (errorCode) {
                        400 -> "Error 400: Solicitud inválida. Verifica el formato de la API key."
                        403 -> "Error 403: API key inválida o sin permisos."
                        404 -> "Error 404: Endpoint no encontrado. Verifica la configuración de la API."
                        429 -> "Error 429: Límite de solicitudes excedido. Espera unos minutos."
                        else -> "Error $errorCode: $errorBody"
                    }
                    Toast.makeText(this@SettingsActivity, message, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("SettingsActivity", "Error al probar API", e)
                val message = when {
                    e.message?.contains("UnknownHostException") == true ||
                    e.message?.contains("Unable to resolve host") == true ->
                        getString(R.string.error_no_internet)
                    else -> "Error: ${e.message}"
                }
                Toast.makeText(this@SettingsActivity, message, Toast.LENGTH_LONG).show()
            }

            binding.btnTest.isEnabled = true
            binding.btnTest.text = getString(R.string.btn_test_api)
        }
    }

    private fun openGeminiWebsite() {
        val url = "https://makersuite.google.com/app/apikey"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
