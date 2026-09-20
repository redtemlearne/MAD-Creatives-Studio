package com.example.data.repository

import android.content.Context
import com.example.data.local.MediaAssetDao
import com.example.data.local.MediaAssetEntity
import com.example.data.local.ProjectDao
import com.example.data.media.UriPermissionHelper
import com.example.model.MediaAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class RoomMediaRepository(
    private val mediaAssetDao: MediaAssetDao,
    private val projectDao: ProjectDao,
    private val context: Context
) : MediaRepository {

    override fun getAssets(projectId: String): Flow<List<MediaAsset>> {
        return mediaAssetDao.getAssetsForProject(projectId).map { entities ->
            entities.map { entity ->
                MediaAsset(
                    id = entity.id,
                    projectId = entity.projectId,
                    sourceUri = entity.sourceUri,
                    displayName = entity.displayName,
                    mimeType = entity.mimeType,
                    sizeBytes = entity.sizeBytes,
                    durationMs = entity.durationMs,
                    width = entity.width,
                    height = entity.height,
                    rotationDegrees = entity.rotationDegrees,
                    addedAt = entity.addedAt,
                    isAvailable = true // Runtime availability set by ViewModel
                )
            }
        }
    }

    override suspend fun getAssetsOnce(projectId: String): List<MediaAsset> = withContext(Dispatchers.IO) {
        mediaAssetDao.getAssetsForProjectOnce(projectId).map { entity ->
            MediaAsset(
                id = entity.id,
                projectId = entity.projectId,
                sourceUri = entity.sourceUri,
                displayName = entity.displayName,
                mimeType = entity.mimeType,
                sizeBytes = entity.sizeBytes,
                durationMs = entity.durationMs,
                width = entity.width,
                height = entity.height,
                rotationDegrees = entity.rotationDegrees,
                addedAt = entity.addedAt,
                isAvailable = true
            )
        }
    }

    override suspend fun addAsset(asset: MediaAsset) = withContext(Dispatchers.IO) {
        val entity = MediaAssetEntity(
            id = asset.id,
            projectId = asset.projectId,
            sourceUri = asset.sourceUri,
            displayName = asset.displayName,
            mimeType = asset.mimeType,
            sizeBytes = asset.sizeBytes,
            durationMs = asset.durationMs,
            width = asset.width,
            height = asset.height,
            rotationDegrees = asset.rotationDegrees,
            addedAt = asset.addedAt
        )
        mediaAssetDao.insertAsset(entity)
        projectDao.updateProjectTimestamp(asset.projectId, asset.addedAt)
    }

    override suspend fun removeAsset(id: String) = withContext(Dispatchers.IO) {
        val asset = mediaAssetDao.getAssetById(id) ?: return@withContext
        mediaAssetDao.deleteAssetById(id)

        // Delete cached thumbnail
        val thumbFile = File(context.cacheDir, "thumbnails/$id.jpg")
        if (thumbFile.exists()) {
            thumbFile.delete()
        }

        // Release URI grant if no other asset uses this URI
        if (mediaAssetDao.countAssetsWithUri(asset.sourceUri) == 0) {
            UriPermissionHelper.releaseGrantIfUnused(context, asset.sourceUri)
        }

        // Update project's updatedAt
        projectDao.updateProjectTimestamp(asset.projectId, System.currentTimeMillis())
    }

    override suspend fun countUriUsage(uri: String): Int = withContext(Dispatchers.IO) {
        mediaAssetDao.countAssetsWithUri(uri)
    }
}
