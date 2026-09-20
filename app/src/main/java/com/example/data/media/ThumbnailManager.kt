package com.example.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

object ThumbnailManager {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = max(maxMemory / 8, 4 * 1024) // At least 4MB or 1/8th of memory

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun getThumbnailFile(context: Context, assetId: String): File {
        val dir = File(context.cacheDir, "thumbnails")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "$assetId.jpg")
    }

    suspend fun getThumbnail(context: Context, assetId: String, sourceUri: String): Bitmap? = withContext(Dispatchers.IO) {
        // Check memory cache first
        memoryCache.get(assetId)?.let { return@withContext it }

        // Check disk cache
        val file = getThumbnailFile(context, assetId)
        if (file.exists() && file.length() > 0) {
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    memoryCache.put(assetId, bitmap)
                    return@withContext bitmap
                }
            } catch (_: Exception) {}
        }

        // Regenerate lazily if missing
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.parse(sourceUri))
            val bitmap = extractAndSaveThumbnail(retriever, file)
            retriever.release()
            if (bitmap != null) {
                memoryCache.put(assetId, bitmap)
                return@withContext bitmap
            }
        } catch (_: Exception) {}

        null
    }

    fun extractAndSaveThumbnail(retriever: MediaMetadataRetriever, outputFile: File): Bitmap? {
        return try {
            outputFile.parentFile?.mkdirs()
            // Frame near start (e.g. 100ms or 0)
            val rawFrame = retriever.getFrameAtTime(100_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST)
                ?: retriever.frameAtTime
                ?: return null

            val maxEdge = max(rawFrame.width, rawFrame.height)
            val scaledBitmap = if (maxEdge > 320) {
                val scale = 320f / maxEdge
                val dstW = (rawFrame.width * scale).toInt().coerceAtLeast(1)
                val dstH = (rawFrame.height * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(rawFrame, dstW, dstH, true)
            } else {
                rawFrame
            }

            FileOutputStream(outputFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            scaledBitmap
        } catch (_: Exception) {
            null
        }
    }
}
