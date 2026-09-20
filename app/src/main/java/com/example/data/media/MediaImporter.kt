package com.example.data.media

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.repository.MediaRepository
import com.example.model.MediaAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class MediaImporter(
    private val context: Context,
    private val mediaRepository: MediaRepository
) {
    data class ImportResult(
        val totalAttempted: Int,
        val importedCount: Int,
        val failedCount: Int
    )

    suspend fun importVideos(
        projectId: String,
        uris: List<Uri>,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): ImportResult = withContext(Dispatchers.IO) {
        val total = uris.size
        var imported = 0
        var failed = 0

        // Retrieve existing URIs in this project to skip duplicates
        val existingAssets = mediaRepository.getAssetsOnce(projectId)
        val existingUris = existingAssets.map { it.sourceUri }.toSet()

        uris.forEachIndexed { index, uri ->
            onProgress(index + 1, total)
            val uriString = uri.toString()

            // Skip duplicate: same URI already in the same project
            if (existingUris.contains(uriString)) {
                // Duplicate in same project is skipped without failure message
                return@forEachIndexed
            }

            // Immediately call takePersistableUriPermission(READ).
            // If persisting fails, report that file as failed and do not add it.
            val permissionGranted = try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                true
            } catch (_: Exception) {
                false
            }

            if (!permissionGranted) {
                failed++
                return@forEachIndexed
            }

            // Extract metadata with MediaMetadataRetriever
            val assetId = UUID.randomUUID().toString()
            val retriever = MediaMetadataRetriever()
            var importSucceeded = false
            try {
                retriever.setDataSource(context, uri)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMs = durationStr?.toLongOrNull() ?: 0L

                // Missing or zero duration = failed import
                if (durationMs <= 0L) {
                    failed++
                    // Release permission since we won't keep this asset
                    if (mediaRepository.countUriUsage(uriString) == 0) {
                        UriPermissionHelper.releaseGrantIfUnused(context, uriString)
                    }
                    return@forEachIndexed
                }

                val rawWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                val rawHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
                val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0

                // Swap width/height for 90/270 rotation
                val width = if (rotation == 90 || rotation == 270) rawHeight else rawWidth
                val height = if (rotation == 90 || rotation == 270) rawWidth else rawHeight

                // Display name and size via OpenableColumns
                var displayName = "Video_${System.currentTimeMillis()}"
                var sizeBytes = 0L

                try {
                    context.contentResolver.query(
                        uri,
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

                val mimeType = context.contentResolver.getType(uri) ?: "video/mp4"

                // Generate thumbnail into cacheDir/thumbnails/{assetId}.jpg
                val thumbFile = ThumbnailManager.getThumbnailFile(context, assetId)
                ThumbnailManager.extractAndSaveThumbnail(retriever, thumbFile)

                val asset = MediaAsset(
                    id = assetId,
                    projectId = projectId,
                    sourceUri = uriString,
                    displayName = displayName,
                    mimeType = mimeType,
                    sizeBytes = sizeBytes,
                    durationMs = durationMs,
                    width = width,
                    height = height,
                    rotationDegrees = rotation,
                    addedAt = System.currentTimeMillis(),
                    isAvailable = true
                )

                mediaRepository.addAsset(asset)
                imported++
                importSucceeded = true
            } catch (_: Exception) {
                failed++
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {}

                if (!importSucceeded) {
                    if (mediaRepository.countUriUsage(uriString) == 0) {
                        UriPermissionHelper.releaseGrantIfUnused(context, uriString)
                    }
                }
            }
        }

        ImportResult(
            totalAttempted = total,
            importedCount = imported,
            failedCount = failed
        )
    }
}
