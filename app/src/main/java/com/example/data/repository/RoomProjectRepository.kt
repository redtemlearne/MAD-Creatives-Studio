package com.example.data.repository

import android.content.Context
import com.example.data.local.MediaAssetDao
import com.example.data.local.ProjectDao
import com.example.data.local.ProjectEntity
import com.example.data.media.UriPermissionHelper
import com.example.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class RoomProjectRepository(
    private val projectDao: ProjectDao,
    private val mediaAssetDao: MediaAssetDao,
    private val context: Context
) : ProjectRepository {

    override fun getProjects(): Flow<List<Project>> {
        return projectDao.getAllProjectsWithStats().map { entities ->
            entities.map { entity ->
                Project(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    aspectRatio = entity.aspectRatio,
                    clipCount = entity.clipCount,
                    firstAssetId = entity.firstAssetId,
                    firstAssetSourceUri = entity.firstAssetSourceUri
                )
            }
        }
    }

    override fun getProject(id: String): Flow<Project?> {
        return projectDao.getProjectWithStatsById(id).map { entity ->
            entity?.let {
                Project(
                    id = it.id,
                    name = it.name,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    aspectRatio = it.aspectRatio,
                    clipCount = it.clipCount,
                    firstAssetId = it.firstAssetId,
                    firstAssetSourceUri = it.firstAssetSourceUri
                )
            }
        }
    }

    override suspend fun getProjectOnce(id: String): Project? = withContext(Dispatchers.IO) {
        projectDao.getProjectWithStatsByIdOnce(id)?.let {
            Project(
                id = it.id,
                name = it.name,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
                aspectRatio = it.aspectRatio,
                clipCount = it.clipCount,
                firstAssetId = it.firstAssetId,
                firstAssetSourceUri = it.firstAssetSourceUri
            )
        }
    }

    override suspend fun createProject(name: String): Project = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val entity = ProjectEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifEmpty { "Untitled Project" },
            createdAt = now,
            updatedAt = now,
            aspectRatio = "9:16"
        )
        projectDao.insertProject(entity)
        Project(
            id = entity.id,
            name = entity.name,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            aspectRatio = entity.aspectRatio,
            clipCount = 0,
            firstAssetId = null
        )
    }

    override suspend fun renameProject(id: String, newName: String) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        projectDao.updateProjectName(id, newName.trim().ifEmpty { "Untitled Project" }, now)
    }

    override suspend fun updateProjectTimestamp(id: String, updatedAt: Long) = withContext(Dispatchers.IO) {
        projectDao.updateProjectTimestamp(id, updatedAt)
    }

    override suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        // Collect distinct URIs and asset IDs in this project before deletion
        val uris = mediaAssetDao.getDistinctUrisForProject(id)
        val assets = mediaAssetDao.getAssetsForProjectOnce(id)

        // Delete project (cascades to assets via foreign key)
        projectDao.deleteProjectById(id)

        // Delete thumbnail cache files
        val thumbDir = File(context.cacheDir, "thumbnails")
        assets.forEach { asset ->
            File(thumbDir, "${asset.id}.jpg").delete()
        }

        // Release URI grants if no other assets across all projects use them
        uris.forEach { uri ->
            if (mediaAssetDao.countAssetsWithUri(uri) == 0) {
                UriPermissionHelper.releaseGrantIfUnused(context, uri)
            }
        }
    }
}
