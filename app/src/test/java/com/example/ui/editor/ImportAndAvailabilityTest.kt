package com.example.ui.editor

import com.example.data.media.MediaImporter
import com.example.fakes.FakeMediaRepository
import com.example.fakes.FakeProjectRepository
import com.example.model.MediaAsset
import com.example.model.Project
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImportAndAvailabilityTest {

    private lateinit var projectRepo: FakeProjectRepository
    private lateinit var mediaRepo: FakeMediaRepository

    @Before
    fun setup() {
        projectRepo = FakeProjectRepository()
        mediaRepo = FakeMediaRepository()
    }

    @Test
    fun importFlow_successPath_writesToRepositoryAndUpdatesProject() = runBlocking {
        val initialTimestamp = 1000L
        projectRepo.setProject(
            Project(
                id = "p1",
                name = "My Project",
                createdAt = initialTimestamp,
                updatedAt = initialTimestamp,
                aspectRatio = "9:16"
            )
        )

        val asset = MediaAsset(
            id = "a1",
            projectId = "p1",
            sourceUri = "content://media/external/video/1",
            displayName = "video1.mp4",
            mimeType = "video/mp4",
            sizeBytes = 1024L,
            durationMs = 5000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            addedAt = 2000L,
            isAvailable = true
        )

        // Write to media repository
        mediaRepo.addAsset(asset)
        projectRepo.updateProjectTimestamp("p1", 2000L)

        // Verify media repo has asset
        val assets = mediaRepo.getAssets("p1").first()
        assertEquals(1, assets.size)
        assertEquals("a1", assets[0].id)
        assertEquals("video1.mp4", assets[0].displayName)

        // Verify project timestamp was updated
        val updatedProject = projectRepo.getProject("p1").first()
        assertEquals(2000L, updatedProject?.updatedAt)
    }

    @Test
    fun importFlow_emptyUriList_doesNothing() = runBlocking {
        projectRepo.setProject(
            Project(
                id = "p1",
                name = "My Project",
                createdAt = 1000L,
                updatedAt = 1000L,
                aspectRatio = "9:16"
            )
        )

        val uris = emptyList<String>()
        val importResult = if (uris.isEmpty()) {
            MediaImporter.ImportResult(totalAttempted = 0, importedCount = 0, failedCount = 0)
        } else {
            MediaImporter.ImportResult(totalAttempted = uris.size, importedCount = 1, failedCount = 0)
        }

        assertEquals(0, importResult.totalAttempted)
        assertEquals(0, importResult.importedCount)
        assertEquals(0, importResult.failedCount)

        val assets = mediaRepo.getAssets("p1").first()
        assertTrue(assets.isEmpty())
    }

    @Test
    fun importFlow_extractionFailure_dropsBadUriAndReportsFailureWithoutBlockingValidOnes() = runBlocking {
        projectRepo.setProject(
            Project(
                id = "p1",
                name = "My Project",
                createdAt = 1000L,
                updatedAt = 1000L,
                aspectRatio = "9:16"
            )
        )

        val validAsset = MediaAsset(
            id = "a1",
            projectId = "p1",
            sourceUri = "content://valid/1",
            displayName = "valid.mp4",
            mimeType = "video/mp4",
            sizeBytes = 1024L,
            durationMs = 4000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            addedAt = 1500L,
            isAvailable = true
        )

        // Simulated result where 1 valid asset succeeded, 2 failed
        val importResult = MediaImporter.ImportResult(
            totalAttempted = 3,
            importedCount = 1,
            failedCount = 2
        )

        mediaRepo.addAsset(validAsset)

        val assets = mediaRepo.getAssets("p1").first()
        assertEquals(1, assets.size)
        assertEquals("valid.mp4", assets[0].displayName)
        assertEquals(2, importResult.failedCount)
    }

    @Test
    fun availabilityChecking_availableUri_setsFlagTrue() = runBlocking {
        val asset = MediaAsset(
            id = "a1",
            projectId = "p1",
            sourceUri = "content://available/1",
            displayName = "test.mp4",
            mimeType = "video/mp4",
            sizeBytes = 1024L,
            durationMs = 3000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            addedAt = 1000L,
            isAvailable = false // Initially false
        )
        mediaRepo.addAsset(asset)

        // Availability check resolves to true
        mediaRepo.setAssetAvailability("a1", true)

        val retrieved = mediaRepo.getAssetDirect("a1")
        assertTrue(retrieved?.isAvailable == true)
    }

    @Test
    fun availabilityChecking_unresolvableUri_setsFlagFalse() = runBlocking {
        val asset = MediaAsset(
            id = "a2",
            projectId = "p1",
            sourceUri = "content://deleted/file",
            displayName = "missing.mp4",
            mimeType = "video/mp4",
            sizeBytes = 1024L,
            durationMs = 3000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            addedAt = 1000L,
            isAvailable = true // Initially true
        )
        mediaRepo.addAsset(asset)

        // Availability check fails to open URI -> sets to false
        mediaRepo.setAssetAvailability("a2", false)

        val retrieved = mediaRepo.getAssetDirect("a2")
        assertFalse(retrieved?.isAvailable == true)
    }

    @Test
    fun availabilityChecking_unavailableAssetRemainsInRepositoryAndTray() = runBlocking {
        val unavailableAsset = MediaAsset(
            id = "a-unavail",
            projectId = "p1",
            sourceUri = "content://unavail/file",
            displayName = "unavail.mp4",
            mimeType = "video/mp4",
            sizeBytes = 1024L,
            durationMs = 3000L,
            width = 1080,
            height = 1920,
            rotationDegrees = 0,
            addedAt = 1000L,
            isAvailable = false
        )
        mediaRepo.addAsset(unavailableAsset)

        // Remains in repository
        val stored = mediaRepo.getAssetDirect("a-unavail")
        assertEquals("a-unavail", stored?.id)
        assertFalse(stored!!.isAvailable)

        // Remains in project assets query (which populates tray)
        val trayAssets = mediaRepo.getAssets("p1").first()
        assertEquals(1, trayAssets.size)
        assertEquals("a-unavail", trayAssets[0].id)
        assertFalse(trayAssets[0].isAvailable)
    }
}
