package com.example.data.media

import com.example.data.repository.MediaRepository
import com.example.data.repository.ProjectRepository
import com.example.model.Availability
import com.example.model.MediaAsset
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class MediaImporter(
    private val videoMetadataReader: VideoMetadataReader,
    private val uriGrantManager: UriGrantManager,
    private val thumbnailGenerator: ThumbnailGenerator,
    private val mediaRepository: MediaRepository,
    private val projectRepository: ProjectRepository? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    data class ImportResult(
        val totalAttempted: Int,
        val importedCount: Int,
        val failedCount: Int,
        val skippedDuplicates: Int = 0
    )

    suspend fun importVideos(
        projectId: String,
        uris: List<String>,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): ImportResult = withContext(dispatcher) {
        val existingAssets = mediaRepository.getAssetsOnce(projectId)
        val seenUris = existingAssets.map { it.sourceUri }.toMutableSet()

        var skippedDuplicates = 0
        val toProcess = mutableListOf<String>()

        for (uri in uris) {
            if (seenUris.contains(uri)) {
                skippedDuplicates++
            } else {
                seenUris.add(uri)
                toProcess.add(uri)
            }
        }

        val totalAttempted = toProcess.size
        var importedCount = 0
        var failedCount = 0

        toProcess.forEachIndexed { index, uri ->
            onProgress(index + 1, totalAttempted)

            val granted = uriGrantManager.persist(uri)
            if (!granted) {
                failedCount++
                return@forEachIndexed
            }

            var assetId: String? = null
            var thumbnailGenerated = false

            try {
                val metadata: VideoMetadata? = videoMetadataReader.read(uri)

                if (metadata == null || metadata.durationMs <= 0L) {
                    failedCount++
                    if (mediaRepository.countUriUsage(uri) == 0) {
                        uriGrantManager.release(uri)
                    }
                    return@forEachIndexed
                }

                val id = UUID.randomUUID().toString()
                assetId = id
                thumbnailGenerated = thumbnailGenerator.generate(id, uri)

                val asset = MediaAsset(
                    id = id,
                    projectId = projectId,
                    sourceUri = uri,
                    displayName = metadata.displayName,
                    mimeType = metadata.mimeType,
                    sizeBytes = metadata.sizeBytes,
                    durationMs = metadata.durationMs,
                    width = metadata.width,
                    height = metadata.height,
                    rotationDegrees = metadata.rotationDegrees,
                    addedAt = System.currentTimeMillis(),
                    availability = Availability.Available
                )

                mediaRepository.addAsset(asset)
                importedCount++
                projectRepository?.updateProjectTimestamp(projectId, System.currentTimeMillis())
            } catch (ce: kotlin.coroutines.cancellation.CancellationException) {
                throw ce
            } catch (e: Exception) {
                failedCount++
                if (assetId != null && thumbnailGenerated) {
                    thumbnailGenerator.deleteThumbnail(assetId)
                }
                try {
                    if (mediaRepository.countUriUsage(uri) == 0) {
                        uriGrantManager.release(uri)
                    }
                } catch (ce: kotlin.coroutines.cancellation.CancellationException) {
                    throw ce
                } catch (_: Exception) {}
            }
        }

        ImportResult(
            totalAttempted = totalAttempted,
            importedCount = importedCount,
            failedCount = failedCount,
            skippedDuplicates = skippedDuplicates
        )
    }
}
