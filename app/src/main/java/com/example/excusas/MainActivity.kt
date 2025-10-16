package com.example.excusas

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.excusas.databinding.ActivityMainBinding
import com.example.excusas.repository.ExcuseRepository
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentExcuse: String = ""
    private var currentIronicPhrase: String = ""
    private var selectedCategory: ExcuseCategory? = null
    private lateinit var repository: ExcuseRepository
    private var isAIMode = false
    private var currentPhotoUri: Uri? = null

    // Launchers para permisos y actividades
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show()
        }
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            processImageFromUri(currentPhotoUri!!)
        } else {
            Toast.makeText(this, R.string.camera_error, Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processImageFromUri(uri)
        } else {
            Toast.makeText(this, R.string.gallery_error, Toast.LENGTH_SHORT).show()
        }
    }

    private val categories = listOf(
        ExcuseCategory("Trabajo", R.array.excuses_work, "💼"),
        ExcuseCategory("Estudio", R.array.excuses_study, "📚"),
        ExcuseCategory("Familia", R.array.excuses_family, "👨‍👩‍👧‍👦"),
        ExcuseCategory("Amigos", R.array.excuses_friends, "🎉"),
        ExcuseCategory("Citas", R.array.excuses_dates, "💕"),
        ExcuseCategory("General", R.array.excuses_general, "🌟")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            repository = ExcuseRepository(this)

            setupCategories()
            setupListeners()
            checkApiKeyStatus()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al iniciar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar el estado de la API key cuando se regresa de configuración
        checkApiKeyStatus()
    }

    private fun checkApiKeyStatus() {
        // Verificar si la API key está configurada al iniciar
        if (!repository.isApiKeyConfigured()) {
            // Deshabilitar el switch de IA si no hay API key
            binding.switchAI.isEnabled = false
            binding.switchAI.isChecked = false
            binding.switchAI.alpha = 0.5f
            isAIMode = false
        } else {
            // Habilitar el switch si hay API key
            binding.switchAI.isEnabled = true
            binding.switchAI.alpha = 1f
        }
    }

    private fun setupCategories() {
        categories.forEach { category ->
            val chip = Chip(this).apply {
                text = category.emoji + " " + category.name
                isCheckable = true
                setOnClickListener {
                    selectedCategory = category
                    binding.tvExcuseEmoji.text = category.emoji
                }
            }
            binding.chipGroupCategories.addView(chip)
        }

        // Seleccionar "General" por defecto
        val generalIndex = categories.indexOfFirst { it.name == "General" }
        if (generalIndex >= 0) {
            (binding.chipGroupCategories.getChildAt(generalIndex) as? Chip)?.isChecked = true
            selectedCategory = categories[generalIndex]
            binding.tvExcuseEmoji.text = categories[generalIndex].emoji
        }
    }

    private fun setupListeners() {
        binding.btnGenerate.setOnClickListener {
            if (isAIMode) {
                generateExcuseWithAI()
            } else {
                generateExcuse()
            }
        }

        binding.btnCopy.setOnClickListener {
            copyExcuseToClipboard()
        }

        binding.btnShare.setOnClickListener {
            shareExcuse()
        }

        binding.btnShareImage.setOnClickListener {
            showMemeOptionsDialog()
        }

        binding.switchAI.setOnCheckedChangeListener { _, isChecked ->
            isAIMode = isChecked
            if (isChecked) {
                binding.switchAI.text = getString(R.string.mode_ai)
                binding.btnGenerate.text = getString(R.string.btn_generate_ai)
            } else {
                binding.switchAI.text = getString(R.string.mode_predefined)
                binding.btnGenerate.text = getString(R.string.btn_generate)
            }
        }

        binding.fabSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.fabAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
    }

    private fun generateExcuse() {
        val category = selectedCategory ?: return

        val excusesArray = resources.getStringArray(category.excusesArrayId)
        if (excusesArray.isEmpty()) return

        val randomIndex = Random.nextInt(excusesArray.size)
        currentExcuse = excusesArray[randomIndex]

        // Generar frase irónica aleatoria
        generateIronicPhrase()

        animateExcuseDisplay(currentExcuse)
    }

    private fun generateIronicPhrase() {
        val ironicPhrases = resources.getStringArray(R.array.ironic_phrases)
        if (ironicPhrases.isNotEmpty()) {
            val randomIndex = Random.nextInt(ironicPhrases.size)
            currentIronicPhrase = ironicPhrases[randomIndex]
        }
    }

    private fun generateExcuseWithAI() {
        val category = selectedCategory ?: return

        // Mostrar estado de carga
        binding.btnGenerate.isEnabled = false
        binding.tvExcuse.text = getString(R.string.generating)

        lifecycleScope.launch {
            val result = repository.generateExcuseWithAI(category.name)

            result.onSuccess { excuse ->
                currentExcuse = excuse
                // Generar frase irónica también en modo IA
                generateIronicPhrase()
                animateExcuseDisplay(excuse)
            }.onFailure { error ->
                // Usar fallback a excusas predefinidas en caso de error
                handleAIError(error, category)
            }

            binding.btnGenerate.isEnabled = true
        }
    }

    private fun handleAIError(error: Throwable, category: ExcuseCategory) {
        val errorMessage = error.message ?: ""

        when {
            errorMessage.contains("API_KEY_NOT_CONFIGURED") -> {
                // API key no configurada - usar predefinidas
                Toast.makeText(this, R.string.api_key_not_configured, Toast.LENGTH_SHORT).show()
                generatePredefinedExcuse(category, showToast = false)
            }
            errorMessage.contains("UnknownHostException") ||
            errorMessage.contains("Unable to resolve host") ||
            errorMessage.contains("SocketTimeoutException") ||
            errorMessage.contains("ConnectException") -> {
                // Sin conexión a internet - usar predefinidas
                Toast.makeText(this, R.string.fallback_to_predefined, Toast.LENGTH_SHORT).show()
                generatePredefinedExcuse(category, showToast = false)
            }
            else -> {
                // Cualquier otro error - usar predefinidas
                Toast.makeText(this, R.string.fallback_to_predefined, Toast.LENGTH_SHORT).show()
                generatePredefinedExcuse(category, showToast = false)
            }
        }
    }

    private fun generatePredefinedExcuse(category: ExcuseCategory, showToast: Boolean = true) {
        val excusesArray = resources.getStringArray(category.excusesArrayId)
        if (excusesArray.isEmpty()) return

        val randomIndex = Random.nextInt(excusesArray.size)
        currentExcuse = excusesArray[randomIndex]

        animateExcuseDisplay(currentExcuse)

        if (showToast) {
            Toast.makeText(this, R.string.fallback_to_predefined, Toast.LENGTH_SHORT).show()
        }
    }

    private fun animateExcuseDisplay(excuse: String) {
        // Actualizar UI con animación
        binding.tvExcuse.alpha = 0f
        binding.tvExcuse.text = excuse
        binding.tvExcuse.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        // Mostrar frase irónica
        binding.tvIronicPhrase.visibility = View.VISIBLE
        binding.tvIronicPhrase.alpha = 0f
        binding.tvIronicPhrase.text = currentIronicPhrase
        binding.tvIronicPhrase.animate()
            .alpha(0.8f)
            .setDuration(500)
            .setStartDelay(300)
            .start()

        // Mostrar botones de acción
        binding.layoutActions.visibility = View.VISIBLE
        binding.layoutActions.alpha = 0f
        binding.layoutActions.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        // Animar el emoji
        binding.tvExcuseEmoji.scaleX = 0.5f
        binding.tvExcuseEmoji.scaleY = 0.5f
        binding.tvExcuseEmoji.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .start()
    }

    private fun copyExcuseToClipboard() {
        if (currentExcuse.isEmpty()) return

        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Excusa", currentExcuse)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, R.string.excuse_copied, Toast.LENGTH_SHORT).show()
    }

    private fun shareExcuse() {
        if (currentExcuse.isEmpty()) return

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, currentExcuse)
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_excuse))
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_excuse)))
    }

    private fun showMemeOptionsDialog() {
        if (currentExcuse.isEmpty()) return

        val options = arrayOf(
            getString(R.string.meme_option_generate),
            getString(R.string.meme_option_gallery),
            getString(R.string.meme_option_camera)
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.create_meme_title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareExcuseAsImage() // Generar automático
                    1 -> checkStoragePermissionAndOpenGallery() // Desde galería
                    2 -> checkCameraPermissionAndOpen() // Tomar foto
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkStoragePermissionAndOpenGallery() {
        when {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ usa READ_MEDIA_IMAGES
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery()
                } else {
                    storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else -> {
                // Android 12 y anteriores
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery()
                } else {
                    storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            currentPhotoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(currentPhotoUri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.camera_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        try {
            pickImageLauncher.launch("image/*")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.gallery_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val cacheDir = File(cacheDir, "camera")
        cacheDir.mkdirs()
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            cacheDir
        )
    }

    private fun processImageFromUri(uri: Uri) {
        Toast.makeText(this, R.string.image_processing, Toast.LENGTH_SHORT).show()

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null) {
                // Crear meme con la imagen del usuario
                val memeBitmap = createMemeWithUserImage(
                    userImage = originalBitmap,
                    excuse = currentExcuse,
                    ironicPhrase = currentIronicPhrase
                )

                // Guardar y compartir
                saveAndShareBitmap(memeBitmap)
            } else {
                Toast.makeText(this, R.string.error_generating_image, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error_generating_image, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createMemeWithUserImage(
        userImage: Bitmap,
        excuse: String,
        ironicPhrase: String
    ): Bitmap {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Redimensionar y centrar la imagen del usuario
        val scaledImage = scaleBitmapToFit(userImage, width, height - 400)
        val imageTop = 100f
        val imageLeft = (width - scaledImage.width) / 2f
        canvas.drawBitmap(scaledImage, imageLeft, imageTop, null)

        // Configurar paint para texto
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.strokeWidth = 8f
        paint.style = Paint.Style.FILL_AND_STROKE

        // Dibujar excusa en la parte superior con fondo
        val topTextY = 70f
        drawTextWithBackground(canvas, excuse, width / 2f, topTextY, paint, width - 100,
            Color.BLACK, 60f)

        // Dibujar frase irónica en la parte inferior
        val bottomTextY = height - 120f
        drawTextWithBackground(canvas, ironicPhrase, width / 2f, bottomTextY, paint, width - 100,
            Color.BLACK, 40f)

        // Marca de agua
        paint.textSize = 24f
        paint.strokeWidth = 0f
        paint.alpha = 150
        canvas.drawText("excusApp", width / 2f, height - 30f, paint)

        return bitmap
    }

    private fun scaleBitmapToFit(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun drawTextWithBackground(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        maxWidth: Int,
        backgroundColor: Int,
        textSize: Float
    ) {
        paint.textSize = textSize

        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        // Dividir texto en líneas
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val bounds = Rect()
            paint.getTextBounds(testLine, 0, testLine.length, bounds)

            if (bounds.width() > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        // Calcular dimensiones del fondo
        val lineHeight = paint.descent() - paint.ascent()
        val totalHeight = lines.size * (lineHeight + 10)
        val backgroundPaint = Paint()
        backgroundPaint.color = backgroundColor
        backgroundPaint.alpha = 180

        // Dibujar fondo
        canvas.drawRect(
            50f,
            y - 20,
            canvas.width - 50f,
            y + totalHeight + 20,
            backgroundPaint
        )

        // Dibujar texto
        var currentY = y + 20
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 6f

        for (line in lines) {
            canvas.drawText(line, x, currentY, paint)
            currentY += lineHeight + 10
        }
    }

    private fun saveAndShareBitmap(bitmap: Bitmap) {
        try {
            val cachePath = File(cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "meme_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            val contentUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_image_title))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_as_image)))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error_generating_image, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareExcuseAsImage() {
        if (currentExcuse.isEmpty()) return

        Toast.makeText(this, R.string.generating_image, Toast.LENGTH_SHORT).show()

        try {
            // Crear imagen con la excusa
            val bitmap = createExcuseImage(
                excuse = currentExcuse,
                emoji = selectedCategory?.emoji ?: "🤔",
                ironicPhrase = currentIronicPhrase
            )

            saveAndShareBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error_generating_image, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createExcuseImage(excuse: String, emoji: String, ironicPhrase: String): Bitmap {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fondo degradado
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = android.graphics.LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                Color.parseColor("#6366F1"),
                Color.parseColor("#8B5CF6"),
                Color.parseColor("#D946EF")
            ),
            null,
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Configurar paint para texto
        paint.shader = null
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.strokeWidth = 2f

        // Dibujar emoji
        paint.textSize = 180f
        canvas.drawText(emoji, width / 2f, 300f, paint)

        // Dibujar excusa (con salto de línea automático)
        paint.textSize = 48f
        paint.style = Paint.Style.FILL
        drawMultilineText(canvas, excuse, width / 2f, 500f, paint, width - 200)

        // Dibujar frase irónica
        paint.textSize = 32f
        paint.alpha = 200
        paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
        drawMultilineText(canvas, ironicPhrase, width / 2f, height - 250f, paint, width - 200)

        // Dibujar marca de agua
        paint.typeface = android.graphics.Typeface.DEFAULT
        paint.textSize = 28f
        paint.alpha = 150
        canvas.drawText("excusApp", width / 2f, height - 100f, paint)

        return bitmap
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        maxWidth: Int
    ) {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        // Dividir texto en líneas
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val bounds = Rect()
            paint.getTextBounds(testLine, 0, testLine.length, bounds)

            if (bounds.width() > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        // Dibujar cada línea
        val lineHeight = paint.descent() - paint.ascent()
        var currentY = y

        for (line in lines) {
            canvas.drawText(line, x, currentY, paint)
            currentY += lineHeight + 10
        }
    }
}
