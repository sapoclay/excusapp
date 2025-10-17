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
import android.util.Log
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
import android.text.InputType
import android.widget.EditText
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentExcuse: String = ""
    private var currentIronicPhrase: String = ""
    private var selectedCategory: ExcuseCategory? = null
    private lateinit var repository: ExcuseRepository
    private var isAIMode = false
    private var currentPhotoUri: Uri? = null
    // Cuando el usuario escribe una excusa y luego selecciona imagen, marcamos esto para evitar el diálogo adicional
    private var pendingCustomExcuse: Boolean = false

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
        // Permitir generar con excusa automática, escribir excusa propia antes de elegir imagen,
        // o seleccionar imagen desde galería/cámara directamente.
        val options = arrayOf(
            getString(R.string.meme_option_generate),
            getString(R.string.write_custom_excuse),
            getString(R.string.meme_option_gallery),
            getString(R.string.meme_option_camera)
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.create_meme_title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareExcuseAsImage() // Generar automático (usa currentExcuse)
                    1 -> showCustomExcuseAndPickSource() // Escribir excusa y luego elegir imagen
                    2 -> checkStoragePermissionAndOpenGallery() // Desde galería
                    3 -> checkCameraPermissionAndOpen() // Tomar foto
                }
            }
            .show()
    }

    private fun showCustomExcuseAndPickSource() {
        val input = EditText(this).apply {
            hint = getString(R.string.hint_custom_excuse)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.enter_custom_excuse)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val custom = input.text.toString().trim()
                if (custom.isNotEmpty()) {
                    // Guardar la excusa temporalmente y marcar que el usuario la ha provisto
                    currentExcuse = custom
                    pendingCustomExcuse = true

                    // Pedir al usuario que elija la fuente de la imagen
                    val sources = arrayOf(getString(R.string.meme_option_gallery), getString(R.string.meme_option_camera))
                    AlertDialog.Builder(this)
                        .setTitle(R.string.select_meme_source)
                        .setItems(sources) { _, which ->
                            when (which) {
                                0 -> checkStoragePermissionAndOpenGallery()
                                1 -> checkCameraPermissionAndOpen()
                            }
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ ->
                            // Si cancela, limpiar la bandera
                            pendingCustomExcuse = false
                        }
                        .show()
                } else {
                    Toast.makeText(this, R.string.custom_excuse_empty, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
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
            // Cargar la imagen corrigiendo la orientación EXIF
            val originalBitmap = loadBitmapWithCorrectOrientation(uri)

            if (originalBitmap != null) {
                // Si el usuario ya ha escrito una excusa antes de elegir imagen, la usamos directamente
                if (pendingCustomExcuse) {
                    pendingCustomExcuse = false
                    proceedWithExcuse(originalBitmap, currentExcuse)
                } else {
                    // Mostrar diálogo para elegir entre excusa generada o escribir propia
                    showExcuseChoiceDialog(originalBitmap)
                }
            } else {
                Toast.makeText(this, R.string.error_generating_image, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error_generating_image, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showExcuseChoiceDialog(userImage: Bitmap) {
        val options = if (currentExcuse.isNotEmpty()) {
            arrayOf(
                getString(R.string.use_generated_excuse),
                getString(R.string.write_custom_excuse)
            )
        } else {
            arrayOf(getString(R.string.write_custom_excuse))
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.choose_excuse_title)
            .setItems(options) { _, which ->
                if (currentExcuse.isNotEmpty() && which == 0) {
                    // Usar excusa generada
                    proceedWithExcuse(userImage, currentExcuse)
                } else {
                    // Escribir excusa personalizada
                    showCustomExcuseDialog(userImage)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showCustomExcuseDialog(userImage: Bitmap) {
        val input = EditText(this).apply {
            hint = getString(R.string.hint_custom_excuse)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.enter_custom_excuse)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val custom = input.text.toString().trim()
                if (custom.isNotEmpty()) {
                    proceedWithExcuse(userImage, custom)
                } else {
                    Toast.makeText(this, R.string.custom_excuse_empty, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    // Cargar un bitmap y corregir rotación según EXIF
    private fun loadBitmapWithCorrectOrientation(uri: Uri): Bitmap? {
        var input1: InputStream? = null
        var input2: InputStream? = null
        return try {
            input1 = contentResolver.openInputStream(uri)
            val exif = input1?.let { ExifInterface(it) }

            input2 = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(input2)

            val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            if (rotation != 0 && bitmap != null) {
                val matrix = Matrix()
                matrix.postRotate(rotation.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try { input1?.close() } catch (_: Exception) {}
            try { input2?.close() } catch (_: Exception) {}
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
        // Reservar menos espacio para texto: la imagen ocupará la mayor parte del lienzo
        val reservedForText = (height * 0.25).toInt() // 25% para textos (arriba + abajo)
        val scaledImage = ImageUtils.scaleBitmapToFit(userImage, width, height - reservedForText)
        val imageTop = 40f
        val imageLeft = (width - scaledImage.width) / 2f
        canvas.drawBitmap(scaledImage, imageLeft, imageTop, null)

        // Configurar paint para texto
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.strokeWidth = 8f
        paint.style = Paint.Style.FILL_AND_STROKE

        // Dibujar excusa en la parte superior con fondo
        val topTextY = 40f
        // Limitar la excusa a un máximo de 4 líneas y 22% de altura
        drawTextWithBackground(canvas, excuse, width / 2f, topTextY, paint, width - 100,
            Color.BLACK, initialTextSize = 60f, maxLines = 4, maxHeightFraction = 0.22f)

        // Dibujar frase irónica en la parte inferior
        val bottomTextY = height - 120f
        drawTextWithBackground(canvas, ironicPhrase, width / 2f, bottomTextY, paint, width - 100,
            Color.BLACK, initialTextSize = 36f, maxLines = 2, maxHeightFraction = 0.12f)

        // Marca de agua
        paint.textSize = 24f
        paint.strokeWidth = 0f
        paint.alpha = 150
        canvas.drawText("excusApp", width / 2f, height - 30f, paint)

        return bitmap
    }

    private fun drawTextWithBackground(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        maxWidth: Int,
        backgroundColor: Int,
        initialTextSize: Float,
        maxLines: Int = 4,
        maxHeightFraction: Float = 0.25f
    ) {
        // Ajustar tamaño de texto para que encaje en maxWidth y no exceda maxLines ni maxHeightFraction
        var textSize = initialTextSize
        paint.textSize = textSize

        var lines = splitTextIntoLines(text, paint, maxWidth)

        val maxAllowedHeight = canvas.height * maxHeightFraction

        // Reducir tamaño hasta que cumpla las restricciones
        var attempt = 0
        while ((lines.size > maxLines || (lines.size * (paint.descent() - paint.ascent())) > maxAllowedHeight) && attempt < 10) {
            textSize *= 0.85f
            paint.textSize = textSize
            lines = splitTextIntoLines(text, paint, maxWidth)
            attempt++
        }

        // Calcular dimensiones del fondo
        val lineHeight = paint.descent() - paint.ascent()
        val totalHeight = lines.size * (lineHeight + 10)
        val backgroundPaint = Paint()
        backgroundPaint.color = backgroundColor
        backgroundPaint.alpha = 200

        // Dibujar fondo con padding
        val left = 50f
        val right = canvas.width - 50f
        val top = y - 20f
        val bottom = y + totalHeight + 20f
        canvas.drawRect(left, top, right, bottom, backgroundPaint)

        // Dibujar texto con sombra en lugar de stroke grueso
        var currentY = y + 20
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 0f
        paint.setShadowLayer(4f, 2f, 2f, Color.BLACK)

        // Si hay más líneas que maxLines, truncar y añadir '...'
        val displayLines = if (lines.size > maxLines) {
            val truncated = lines.take(maxLines).toMutableList()
            var last = truncated.last()
            // Añadir puntos suspensivos si es necesario
            if (!last.endsWith("...")) last = if (last.length > 3) last.dropLast(3) + "..." else last + "..."
            truncated[truncated.size - 1] = last
            truncated
        } else lines

        for (line in displayLines) {
            canvas.drawText(line, x, currentY, paint)
            currentY += lineHeight + 10
        }

        // Limpiar la sombra para que no afecte otros dibujos
        paint.clearShadowLayer()
    }

    private fun splitTextIntoLines(text: String, paint: Paint, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

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
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
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

    private fun proceedWithExcuse(userImage: Bitmap, excuseText: String) {
        try {
            Log.d("MainActivity", "Proceeding with excuse: $excuseText")
            Log.d("MainActivity", "Bitmap size: ${userImage.width}x${userImage.height}")

            // Guardar el bitmap original en cache y abrir la PreviewActivity para editar/posicionar la excusa
            val uri = saveBitmapToCacheAndGetUri(userImage)
            if (uri != null) {
                Log.d("MainActivity", "Bitmap saved to URI: $uri")

                val intent = Intent(this, PreviewActivity::class.java).apply {
                    putExtra("image_uri", uri.toString())
                    putExtra("excuse_text", excuseText)
                    putExtra("ironic_text", currentIronicPhrase)
                    // Otorgar permisos de lectura para el URI
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(intent)
            } else {
                Log.e("MainActivity", "Failed to save bitmap to cache")
                Toast.makeText(this, R.string.error_generating_image, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in proceedWithExcuse", e)
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Guarda un bitmap temporal en cache/images y devuelve un content Uri a través de FileProvider
    private fun saveBitmapToCacheAndGetUri(bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(cacheDir, "images")
            if (!cachePath.exists()) {
                cachePath.mkdirs()
                Log.d("MainActivity", "Created cache directory: ${cachePath.absolutePath}")
            }

            val file = File(cachePath, "temp_image_${System.currentTimeMillis()}.png")
            Log.d("MainActivity", "Saving to file: ${file.absolutePath}")

            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            Log.d("MainActivity", "File saved successfully, size: ${file.length()} bytes")

            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            Log.d("MainActivity", "FileProvider URI created: $uri")

            uri
        } catch (e: Exception) {
            Log.e("MainActivity", "Error saving bitmap to cache", e)
            e.printStackTrace()
            null
        }
    }
}
