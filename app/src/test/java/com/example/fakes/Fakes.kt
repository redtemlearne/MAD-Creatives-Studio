package com.example.fakes

import com.example.data.media.MediaImporter
import com.example.data.media.ThumbnailGenerator
import com.example.data.media.UriAvailabilityChecker
import com.example.data.media.UriGrantManager
import com.example.data.media.VideoMetadata
import com.example.data.media.VideoMetadataReader
import com.example.data.repository.MediaRepository
import com.example.data.repository.ProjectRepository
import com.example.di.AppContainer
import com.example.model.Availability
import com.example.model.MediaAsset
import com.example.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class FakeVideoMetadataReader : VideoMetadataReader {
    val readCalls = mutableListOf<String>()
    var metadataMap = mutableMapOf<String, VideoMetadata?>()
    var exceptionToThrow: Exception? = null

    override fun read(uri: String): VideoMetadata? {
        readCalls.add(uri)
        exceptionToThrow?.let { throw it }
        return metadataMap[uri] ?: VideoMetadata(
            displayName = "Test_Video",
            mimeType = "video/mp4",
            sizeBytes = 1024L,
            durationMs = 5000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0
        )
    }
}

class FakeUriGrantManager : UriGrantManager {
    val persistCalls = mutableListOf<String>()
    val releasedUris = mutableListOf<String>()
    var persistSucceeds = mutableMapOf<String, Boolean>()

    override fun persist(uri: String): Boolean {
        persistCalls.add(uri)
        return persistSucceeds[uri] ?: true
    }

    override fun release(uri: String) {
        releasedUris.add(uri)
    }
}

class FakeThumbnailGenerator : ThumbnailGenerator {
    val generateCalls = mutableListOf<Pair<String, String>>()
    var shouldSucceed = true

    override fun generate(assetId: String, uri: String): Boolean {
        generateCalls.add(assetId to uri)
        return shouldSucceed
    }
}

class FakeUriAvailabilityChecker : UriAvailabilityChecker {
    val availabilityMap = mutableMapOf<String, Boolean>()
    var defaultAvailability = true

    override suspend fun isAvailable(uri: String): Boolean {
        return availabilityMap[uri] ?: defaultAvailability
    }
}

class FakeProjectRepository : ProjectRepository {
    private val projectsMap = MutableStateFlow<Map<String, Project>>(emptyMap())

    override fun getProjects(): Flow<List<Project>> {
        return projectsMap.map { it.values.sortedByDescending { p -> p.updatedAt } }
    }

    override fun getProject(id: String): Flow<Project?> {
        return projectsMap.map { it[id] }
    }

    override suspend fun getProjectOnce(id: String): Project? {
        return projectsMap.value[id]
    }

    override suspend fun createProject(name: String): Project {
        val now = System.currentTimeMillis()
        val project = Project(
            id = UUID.randomUUID().toString(),
            name = name,
            createdAt = now,
            updatedAt = now,
            aspectRatio = "9:16",
            clipCount = 0,
            firstAssetId = null,
            firstAssetSourceUri = null
        )
        projectsMap.value = projectsMap.value + (project.id to project)
        return project
    }

    override suspend fun renameProject(id: String, newName: String) {
        val current = projectsMap.value[id] ?: return
        projectsMap.value = projectsMap.value + (id to current.copy(name = newName, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun updateProjectTimestamp(id: String, updatedAt: Long) {
        val current = projectsMap.value[id] ?: return
        projectsMap.value = projectsMap.value + (id to current.copy(updatedAt = updatedAt))
    }

    override suspend fun deleteProject(id: String) {
        projectsMap.value = projectsMap.value - id
    }

    fun setProject(project: Project) {
        projectsMap.value = projectsMap.value + (project.id to project)
    }
}

class FakeMediaRepository : MediaRepository {
    private val assetsMap = MutableStateFlow<Map<String, MediaAsset>>(emptyMap())

    override fun getAssets(projectId: String): Flow<List<MediaAsset>> {
        return assetsMap.map { map ->
            map.values.filter { it.projectId == projectId }.sortedBy { it.addedAt }
        }
    }

    override suspend fun getAssetsOnce(projectId: String): List<MediaAsset> {
        return assetsMap.value.values.filter { it.projectId == projectId }.sortedBy { it.addedAt }
    }

    override suspend fun addAsset(asset: MediaAsset) {
        assetsMap.value = assetsMap.value + (asset.id to asset)
    }

    override suspend fun removeAsset(id: String) {
        assetsMap.value = assetsMap.value - id
    }

    override suspend fun countUriUsage(uri: String): Int {
        return assetsMap.value.values.count { it.sourceUri == uri }
    }

    fun setAssetAvailability(id: String, availability: Availability) {
        val current = assetsMap.value[id] ?: return
        assetsMap.value = assetsMap.value + (id to current.copy(availability = availability))
    }

    fun getAssetDirect(id: String): MediaAsset? {
        return assetsMap.value[id]
    }
}

class TestAppContainer(
    override val projectRepository: ProjectRepository = FakeProjectRepository(),
    override val mediaRepository: MediaRepository = FakeMediaRepository(),
    override val videoMetadataReader: VideoMetadataReader = FakeVideoMetadataReader(),
    override val uriGrantManager: UriGrantManager = FakeUriGrantManager(),
    override val thumbnailGenerator: ThumbnailGenerator = FakeThumbnailGenerator(),
    override val uriAvailabilityChecker: UriAvailabilityChecker = FakeUriAvailabilityChecker(),
    override val mediaImporter: MediaImporter = MediaImporter(
        videoMetadataReader = videoMetadataReader,
        uriGrantManager = uriGrantManager,
        thumbnailGenerator = thumbnailGenerator,
        mediaRepository = mediaRepository,
        projectRepository = projectRepository
    )
) : AppContainer
