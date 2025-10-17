package com.example.excusas

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream

object ImageUtils {
    // Cargar bitmap desde Uri y corregir orientación EXIF
    fun loadBitmapWithCorrectOrientation(contentResolver: ContentResolver, uri: android.net.Uri): Bitmap? {
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

    // Escalar bitmap para caber en maxWidth x maxHeight manteniendo ratio
    fun scaleBitmapToFit(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    // Crear meme con posición configurable: "TOP" o "BOTTOM"
    fun createMeme(
        userImage: Bitmap,
        excuse: String,
        ironicPhrase: String,
        positionTop: Boolean = true,
        outputWidth: Int = 1080,
        outputHeight: Int = 1080
    ): Bitmap {
        val width = outputWidth
        val height = outputHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Reservar menos espacio para texto: 15% para textos (arriba + abajo)
        val reservedForText = (height * 0.15).toInt()
        val scaledImage = scaleBitmapToFit(userImage, width, height - reservedForText)
        val imageTop = 40f
        val imageLeft = (width - scaledImage.width) / 2f
        canvas.drawBitmap(scaledImage, imageLeft, imageTop, null)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.strokeWidth = 8f
        paint.style = Paint.Style.FILL_AND_STROKE

        // Si positionTop true => excusa arriba, irónica abajo; si false => excusa abajo, irónica arriba
        if (positionTop) {
            drawTextWithBackground(canvas, excuse, width / 2f, 40f, paint, width - 100,
                Color.BLACK, initialTextSize = 48f, maxLines = 3, maxHeightFraction = 0.18f)
            drawTextWithBackground(canvas, ironicPhrase, width / 2f, height - 120f, paint, width - 100,
                Color.BLACK, initialTextSize = 32f, maxLines = 2, maxHeightFraction = 0.12f)
        } else {
            drawTextWithBackground(canvas, ironicPhrase, width / 2f, 40f, paint, width - 100,
                Color.BLACK, initialTextSize = 32f, maxLines = 2, maxHeightFraction = 0.12f)
            drawTextWithBackground(canvas, excuse, width / 2f, height - 120f, paint, width - 100,
                Color.BLACK, initialTextSize = 48f, maxLines = 3, maxHeightFraction = 0.18f)
        }

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
        var textSize = initialTextSize
        paint.textSize = textSize

        var lines = splitTextIntoLines(text, paint, maxWidth)
        val maxAllowedHeight = canvas.height * maxHeightFraction

        var attempt = 0
        while ((lines.size > maxLines || (lines.size * (paint.descent() - paint.ascent())) > maxAllowedHeight) && attempt < 10) {
            textSize *= 0.85f
            paint.textSize = textSize
            lines = splitTextIntoLines(text, paint, maxWidth)
            attempt++
        }

        val lineHeight = paint.descent() - paint.ascent()
        val totalHeight = lines.size * (lineHeight + 10)
        val backgroundPaint = Paint()
        backgroundPaint.color = backgroundColor
        backgroundPaint.alpha = 200

        val left = 50f
        val right = canvas.width - 50f
        val top = y - 20f
        val bottom = y + totalHeight + 20f
        canvas.drawRect(left, top, right, bottom, backgroundPaint)

        var currentY = y + 20

        // Configurar el texto con sombra en lugar de stroke grueso
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 0f
        paint.setShadowLayer(4f, 2f, 2f, Color.BLACK)

        val displayLines = if (lines.size > maxLines) {
            val truncated = lines.take(maxLines).toMutableList()
            var last = truncated.last()
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
}
