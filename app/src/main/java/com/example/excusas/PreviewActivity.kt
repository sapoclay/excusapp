package com.example.excusas

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.excusas.databinding.ActivityPreviewBinding
import java.io.File
import java.io.FileOutputStream

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding
    private var originalBitmap: Bitmap? = null
    private var previewBitmap: Bitmap? = null
    private var excuseText: String = ""
    private var ironicText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.preview_title)

        // Inicialmente deshabilitar el botón de compartir hasta generar la preview
        binding.btnShareMeme.isEnabled = false

        // Obtener datos del intent
        val imageUriString = intent.getStringExtra("image_uri")
        excuseText = intent.getStringExtra("excuse_text") ?: ""
        ironicText = intent.getStringExtra("ironic_text") ?: ""

        Log.d("PreviewActivity", "Image URI: $imageUriString")
        Log.d("PreviewActivity", "Excuse: $excuseText")
        Log.d("PreviewActivity", "Ironic: $ironicText")

        if (imageUriString != null) {
            try {
                val imageUri = Uri.parse(imageUriString)
                Log.d("PreviewActivity", "Parsed URI: $imageUri")

                originalBitmap = ImageUtils.loadBitmapWithCorrectOrientation(contentResolver, imageUri)

                if (originalBitmap != null) {
                    Log.d("PreviewActivity", "Bitmap loaded: ${originalBitmap!!.width}x${originalBitmap!!.height}")
                    binding.ivPreviewImage.setImageBitmap(originalBitmap)
                    binding.etExcuse.setText(excuseText)
                } else {
                    Log.e("PreviewActivity", "Failed to load bitmap from URI")
                    Toast.makeText(this, R.string.error_generating_image, Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("PreviewActivity", "Error loading image", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        } else {
            Log.e("PreviewActivity", "Image URI is null")
            Toast.makeText(this, R.string.error_generating_image, Toast.LENGTH_SHORT).show()
            finish()
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnGeneratePreview.setOnClickListener {
            generatePreview()
        }

        binding.btnShareMeme.setOnClickListener {
            sharePreviewMeme()
        }
    }

    private fun generatePreview() {
        val bitmap = originalBitmap
        if (bitmap == null) {
            Toast.makeText(this, R.string.error_generating_image, Toast.LENGTH_SHORT).show()
            return
        }

        val excuse = binding.etExcuse.text.toString().trim()

        if (excuse.isEmpty()) {
            Toast.makeText(this, R.string.custom_excuse_empty, Toast.LENGTH_SHORT).show()
            return
        }

        val positionTop = binding.rbTop.isChecked

        Toast.makeText(this, R.string.generating_image, Toast.LENGTH_SHORT).show()

        try {
            Log.d("PreviewActivity", "Generating meme with excuse: $excuse, positionTop: $positionTop")

            // Usar ImageUtils para crear el meme
            previewBitmap = ImageUtils.createMeme(
                userImage = bitmap,
                excuse = excuse,
                ironicPhrase = ironicText,
                positionTop = positionTop
            )

            Log.d("PreviewActivity", "Meme generated successfully")

            // Mostrar la previsualización
            binding.ivPreviewImage.setImageBitmap(previewBitmap)

            // Habilitar el botón de compartir
            binding.btnShareMeme.isEnabled = true

            Toast.makeText(this, "¡Previsualización generada!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("PreviewActivity", "Error generating preview", e)
            e.printStackTrace()
            Toast.makeText(this, "Error al generar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sharePreviewMeme() {
        val bitmap = previewBitmap ?: originalBitmap

        if (bitmap == null) {
            Toast.makeText(this, R.string.error_generating_image, Toast.LENGTH_SHORT).show()
            return
        }

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
            Log.e("PreviewActivity", "Error sharing meme", e)
            e.printStackTrace()
            Toast.makeText(this, "Error al compartir: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        originalBitmap?.recycle()
        previewBitmap?.recycle()
    }
}
