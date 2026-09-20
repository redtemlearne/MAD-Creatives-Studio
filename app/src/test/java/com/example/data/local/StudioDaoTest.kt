package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudioDaoTest {

    private lateinit var database: StudioDatabase
    private lateinit var projectDao: ProjectDao
    private lateinit var mediaAssetDao: MediaAssetDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, StudioDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        projectDao = database.projectDao()
        mediaAssetDao = database.mediaAssetDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertProject_insertAsset_queryProjectWithAssets_queryStats() = runBlocking {
        val project = ProjectEntity(
            id = "proj-1",
            name = "Travel Vlog",
            createdAt = 1000L,
            updatedAt = 1000L,
            aspectRatio = "9:16"
        )
        projectDao.insertProject(project)

        val asset1 = MediaAssetEntity(
            id = "asset-1",
            projectId = "proj-1",
            sourceUri = "content://media/1",
            displayName = "clip1.mp4",
            mimeType = "video/mp4",
            sizeBytes = 1024L,
            durationMs = 5000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            addedAt = 1000L
        )
        val asset2 = MediaAssetEntity(
            id = "asset-2",
            projectId = "proj-1",
            sourceUri = "content://media/2",
            displayName = "clip2.mp4",
            mimeType = "video/mp4",
            sizeBytes = 2048L,
            durationMs = 8000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            addedAt = 2000L
        )

        mediaAssetDao.insertAsset(asset1)
        mediaAssetDao.insertAsset(asset2)

        // Query project entity
        val retrievedProject = projectDao.getProjectEntityById("proj-1")
        assertNotNull(retrievedProject)
        assertEquals("Travel Vlog", retrievedProject?.name)

        // Query assets by project
        val assets = mediaAssetDao.getAssetsForProject("proj-1").first()
        assertEquals(2, assets.size)
        assertEquals("asset-1", assets[0].id)
        assertEquals("asset-2", assets[1].id)

        // Query stats
        val stats = projectDao.getAllProjectsWithStats().first()
        assertEquals(1, stats.size)
        assertEquals("proj-1", stats[0].id)
        assertEquals(2, stats[0].clipCount)
        assertEquals("asset-1", stats[0].firstAssetId)
        assertEquals("content://media/1", stats[0].firstAssetSourceUri)
    }

    @Test
    fun cascadeDeleteOfProject_deletesItsAssets() = runBlocking {
        val project = ProjectEntity(
            id = "proj-del",
            name = "To Delete",
            createdAt = 1000L,
            updatedAt = 1000L,
            aspectRatio = "9:16"
        )
        projectDao.insertProject(project)

        val asset = MediaAssetEntity(
            id = "asset-del",
            projectId = "proj-del",
            sourceUri = "content://media/del",
            displayName = "del.mp4",
            mimeType = "video/mp4",
            sizeBytes = 1024L,
            durationMs = 3000L,
            width = 720,
            height = 1280,
            rotationDegrees = 0,
            addedAt = 1000L
        )
        mediaAssetDao.insertAsset(asset)

        // Verify inserted
        assertEquals(1, mediaAssetDao.getAssetsForProject("proj-del").first().size)

        // Delete project
        projectDao.deleteProjectById("proj-del")

        // Verify project deleted
        assertNull(projectDao.getProjectEntityById("proj-del"))

        // Verify assets cascaded
        val remainingAssets = mediaAssetDao.getAssetsForProject("proj-del").first()
        assertTrue(remainingAssets.isEmpty())
    }

    @Test
    fun deleteAsset_leavesProjectIntact() = runBlocking {
        val project = ProjectEntity(
            id = "proj-keep",
            name = "Keep Me",
            createdAt = 1000L,
            updatedAt = 1000L,
            aspectRatio = "9:16"
        )
        projectDao.insertProject(project)

        val asset = MediaAssetEntity(
            id = "asset-rem",
            projectId = "proj-keep",
            sourceUri = "content://media/rem",
            displayName = "rem.mp4",
            mimeType = "video/mp4",
            sizeBytes = 1024L,
            durationMs = 4000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            addedAt = 1000L
        )
        mediaAssetDao.insertAsset(asset)

        // Delete asset
        mediaAssetDao.deleteAssetById("asset-rem")

        // Asset gone
        assertNull(mediaAssetDao.getAssetById("asset-rem"))

        // Project intact
        val retrievedProject = projectDao.getProjectEntityById("proj-keep")
        assertNotNull(retrievedProject)
        assertEquals("Keep Me", retrievedProject?.name)
    }

    @Test
    fun duplicateAssetInsert_sameId_replaces() = runBlocking {
        val project = ProjectEntity(
            id = "proj-dup",
            name = "Project",
            createdAt = 1000L,
            updatedAt = 1000L,
            aspectRatio = "9:16"
        )
        projectDao.insertProject(project)

        val assetOriginal = MediaAssetEntity(
            id = "asset-dup",
            projectId = "proj-dup",
            sourceUri = "content://media/original",
            displayName = "original.mp4",
            mimeType = "video/mp4",
            sizeBytes = 1024L,
            durationMs = 2000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            addedAt = 1000L
        )
        mediaAssetDao.insertAsset(assetOriginal)

        val assetReplacement = MediaAssetEntity(
            id = "asset-dup",
            projectId = "proj-dup",
            sourceUri = "content://media/replacement",
            displayName = "replacement.mp4",
            mimeType = "video/mp4",
            sizeBytes = 2048L,
            durationMs = 6000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            addedAt = 2000L
        )
        mediaAssetDao.insertAsset(assetReplacement)

        val assets = mediaAssetDao.getAssetsForProject("proj-dup").first()
        assertEquals(1, assets.size)
        assertEquals("replacement.mp4", assets[0].displayName)
        assertEquals(6000L, assets[0].durationMs)
        assertEquals(2048L, assets[0].sizeBytes)
    }
}
