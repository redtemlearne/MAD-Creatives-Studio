package com.example.data.media

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns

class AndroidVideoMetadataReader(
    private val context: Context
) : VideoMetadataReader {
    override fun read(uri: String): VideoMetadata? {
        val parsedUri = Uri.parse(uri)
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, parsedUri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L

            val rawWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val rawHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0

            val width = if (rotation == 90 || rotation == 270) rawHeight else rawWidth
            val height = if (rotation == 90 || rotation == 270) rawWidth else rawHeight

            var displayName = "Video_${System.currentTimeMillis()}"
            var sizeBytes = 0L

            try {
                context.contentResolver.query(
                    parsedUri,
                    arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1) {
                            cursor.getString(nameIdx)?.let { displayName = it }
                        }
                        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIdx != -1) {
                            sizeBytes = cursor.getLong(sizeIdx)
                        }
                    }
                }
            } catch (_: Exception) {}

            val mimeType = context.contentResolver.getType(parsedUri) ?: "video/mp4"

            VideoMetadata(
                displayName = displayName,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                durationMs = durationMs,
                width = width,
                height = height,
                rotationDegrees = rotation
            )
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }
}
