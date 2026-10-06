package com.moukim.shjirati.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

object ImageUtils {

    fun createTempImageUri(context: Context): Pair<File, Uri>? {
        return runCatching {
            val tempFile = File.createTempFile("temp_plant_camera_", ".jpg", context.cacheDir)
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, tempFile)
            Pair(tempFile, uri)
        }.getOrNull()
    }

    fun saveAndCompressImage(context: Context, sourceUri: Uri, maxDimension: Int = 1280, quality: Int = 80): String? {
        return runCatching {
            val contentResolver = context.contentResolver

            // 1. Determine EXIF rotation
            var rotationDegrees = 0
            runCatching {
                contentResolver.openInputStream(sourceUri)?.use { stream ->
                    val exifInterface = ExifInterface(stream)
                    val orientation = exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    rotationDegrees = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }
            }

            // 2. Decode image bounds to calculate inSampleSize
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            if (originalWidth <= 0 || originalHeight <= 0) return null

            var sampleSize = 1
            if (originalWidth > maxDimension || originalHeight > maxDimension) {
                val halfWidth = originalWidth / 2
                val halfHeight = originalHeight / 2
                while ((halfWidth / sampleSize) >= maxDimension || (halfHeight / sampleSize) >= maxDimension) {
                    sampleSize *= 2
                }
            }

            // 3. Decode bitmap with calculated sampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val decodedBitmap = contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            // 4. Apply rotation if needed
            val rotatedBitmap = if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotated = Bitmap.createBitmap(
                    decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true
                )
                if (rotated != decodedBitmap) {
                    decodedBitmap.recycle()
                }
                rotated
            } else {
                decodedBitmap
            }

            // 5. Scale down if still exceeds maxDimension
            val currentWidth = rotatedBitmap.width
            val currentHeight = rotatedBitmap.height
            val finalBitmap = if (currentWidth > maxDimension || currentHeight > maxDimension) {
                val scale = maxDimension.toFloat() / max(currentWidth, currentHeight)
                val newW = (currentWidth * scale).toInt()
                val newH = (currentHeight * scale).toInt()
                val scaled = Bitmap.createScaledBitmap(rotatedBitmap, newW, newH, true)
                if (scaled != rotatedBitmap) {
                    rotatedBitmap.recycle()
                }
                scaled
            } else {
                rotatedBitmap
            }

            // 6. Save compressed image into app internal filesDir
            val outputFile = File(context.filesDir, "plant_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outputFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            finalBitmap.recycle()

            outputFile.absolutePath
        }.getOrNull()
    }
}
