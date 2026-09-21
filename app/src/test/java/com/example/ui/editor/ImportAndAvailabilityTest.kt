package com.example.ui.editor

import com.example.data.media.MediaImporter
import com.example.data.media.VideoMetadata
import com.example.fakes.FakeMediaRepository
import com.example.fakes.FakeProjectRepository
import com.example.fakes.FakeThumbnailGenerator
import com.example.fakes.FakeUriAvailabilityChecker
import com.example.fakes.FakeUriGrantManager
import com.example.fakes.FakeVideoMetadataReader
import com.example.model.Availability
import com.example.model.MediaAsset
import com.example.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImportAndAvailabilityTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var projectRepo: FakeProjectRepository
    private lateinit var mediaRepo: FakeMediaRepository
    private lateinit var metadataReader: FakeVideoMetadataReader
    private lateinit var grantManager: FakeUriGrantManager
    private lateinit var thumbnailGenerator: FakeThumbnailGenerator
    private lateinit var availabilityChecker: FakeUriAvailabilityChecker
    private lateinit var importer: MediaImporter

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        projectRepo = FakeProjectRepository()
        mediaRepo = FakeMediaRepository()
        metadataReader = FakeVideoMetadataReader()
        grantManager = FakeUriGrantManager()
        thumbnailGenerator = FakeThumbnailGenerator()
        availabilityChecker = FakeUriAvailabilityChecker()
        importer = MediaImporter(
            videoMetadataReader = metadataReader,
            uriGrantManager = grantManager,
            thumbnailGenerator = thumbnailGenerator,
            mediaRepository = mediaRepo,
            projectRepository = projectRepo,
            dispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- MediaImporter Tests ---

    @Test
    fun importer_success_writesAssetAndBumpsProjectUpdatedAt() = runTest(testDispatcher) {
        val initialTime = 1000L
        projectRepo.setProject(
            Project(
                id = "p1",
                name = "Test Project",
                createdAt = initialTime,
                updatedAt = initialTime,
                aspectRatio = "9:16"
            )
        )

        val result = importer.importVideos("p1", listOf("content://media/1"))

        assertEquals(1, result.totalAttempted)
        assertEquals(1, result.importedCount)
        assertEquals(0, result.failedCount)
        assertEquals(0, result.skippedDuplicates)

        val assets = mediaRepo.getAssets("p1").first()
        assertEquals(1, assets.size)
        assertEquals("content://media/1", assets[0].sourceUri)
        assertEquals(Availability.Available, assets[0].availability)

        val updatedProject = projectRepo.getProjectOnce("p1")
        assertNotNull(updatedProject)
        assertTrue(updatedProject!!.updatedAt > initialTime)
    }

    @Test
    fun importer_duplicateUri_skippedAndNotCountedAsFailure() = runTest(testDispatcher) {
        projectRepo.setProject(
            Project(
                id = "p1",
                name = "Test Project",
                createdAt = 1000L,
                updatedAt = 1000L,
                aspectRatio = "9:16"
            )
        )

        // First import
        importer.importVideos("p1", listOf("content://media/1"))

        // Duplicate import
        val duplicateResult = importer.importVideos("p1", listOf("content://media/1"))

        assertEquals(0, duplicateResult.totalAttempted)
        assertEquals(0, duplicateResult.importedCount)
        assertEquals(0, duplicateResult.failedCount)
        assertEquals(1, duplicateResult.skippedDuplicates)

        val assets = mediaRepo.getAssets("p1").first()
        assertEquals(1, assets.size)
    }

    @Test
    fun importer_persistFailure_failedMetadataNeverReadNoAsset() = runTest(testDispatcher) {
        grantManager.persistSucceeds["content://media/unauthorized"] = false

        val result = importer.importVideos("p1", listOf("content://media/unauthorized"))

        assertEquals(1, result.totalAttempted)
        assertEquals(0, result.importedCount)
        assertEquals(1, result.failedCount)
        assertEquals(0, result.skippedDuplicates)

        assertFalse(metadataReader.readCalls.contains("content://media/unauthorized"))
        val assets = mediaRepo.getAssets("p1").first()
        assertTrue(assets.isEmpty())
    }

    @Test
    fun importer_metadataException_failedNoAssetGrantReleased() = runTest(testDispatcher) {
        metadataReader.exceptionToThrow = IllegalStateException("Corrupt header")

        val result = importer.importVideos("p1", listOf("content://media/corrupt"))

        assertEquals(1, result.totalAttempted)
        assertEquals(0, result.importedCount)
        assertEquals(1, result.failedCount)

        val assets = mediaRepo.getAssets("p1").first()
        assertTrue(assets.isEmpty())
        assertTrue(grantManager.releasedUris.contains("content://media/corrupt"))
    }

    @Test
    fun importer_zeroDuration_failedGrantReleased() = runTest(testDispatcher) {
        metadataReader.metadataMap["content://media/zero_duration"] = VideoMetadata(
            displayName = "empty.mp4",
            mimeType = "video/mp4",
            sizeBytes = 1024L,
            durationMs = 0L,
            width = 1920,
            height = 1080,
            rotationDegrees = 0
        )

        val result = importer.importVideos("p1", listOf("content://media/zero_duration"))

        assertEquals(1, result.totalAttempted)
        assertEquals(0, result.importedCount)
        assertEquals(1, result.failedCount)

        val assets = mediaRepo.getAssets("p1").first()
        assertTrue(assets.isEmpty())
        assertTrue(grantManager.releasedUris.contains("content://media/zero_duration"))
    }

    @Test
    fun importer_grantNotReleasedWhenAnotherAssetUsesSameUri() = runTest(testDispatcher) {
        // Existing asset uses uri_shared
        mediaRepo.addAsset(
            MediaAsset(
                id = "existing_asset",
                projectId = "p2",
                sourceUri = "content://media/shared",
                displayName = "shared.mp4",
                mimeType = "video/mp4",
                sizeBytes = 1024L,
                durationMs = 5000L,
                width = 1080,
                height = 1920,
                rotationDegrees = 0,
                addedAt = 1000L
            )
        )

        // Metadata fails for this import of the same URI
        metadataReader.exceptionToThrow = RuntimeException("Temporary read failure")

        val result = importer.importVideos("p1", listOf("content://media/shared"))

        assertEquals(1, result.totalAttempted)
        assertEquals(1, result.failedCount)

        // Grant must NOT be released because p2 still uses this URI!
        assertFalse(grantManager.releasedUris.contains("content://media/shared"))
    }

    @Test
    fun importer_mixedBatch_returnsCorrectCounts() = runTest(testDispatcher) {
        // Set up existing asset for duplicate check
        mediaRepo.addAsset(
            MediaAsset(
                id = "existing",
                projectId = "p1",
                sourceUri = "content://media/existing",
                displayName = "existing.mp4",
                mimeType = "video/mp4",
                sizeBytes = 1024L,
                durationMs = 5000L,
                width = 1080,
                height = 1920,
                rotationDegrees = 0,
                addedAt = 1000L
            )
        )

        grantManager.persistSucceeds["content://media/no_grant"] = false

        val uris = listOf(
            "content://media/existing", // duplicate -> skipped, not counted in totalAttempted
            "content://media/valid",    // valid -> imported
            "content://media/no_grant"  // persist failure -> failed
        )

        val result = importer.importVideos("p1", uris)

        assertEquals(2, result.totalAttempted)
        assertEquals(1, result.importedCount)
        assertEquals(1, result.failedCount)
        assertEquals(1, result.skippedDuplicates)
    }

    @Test
    fun importer_addAssetThrows_failedCountOneNoAssetGrantReleasedAndNoExceptionEscapes() = runTest(testDispatcher) {
        mediaRepo.exceptionToAddAsset = RuntimeException("Database constraint violation")

        val result = importer.importVideos("p1", listOf("content://media/throw_on_add"))

        assertEquals(1, result.totalAttempted)
        assertEquals(0, result.importedCount)
        assertEquals(1, result.failedCount)

        // No asset was added to repository
        val assets = mediaRepo.getAssets("p1").first()
        assertTrue(assets.isEmpty())

        // Grant released since no other asset uses this URI
        assertTrue(grantManager.releasedUris.contains("content://media/throw_on_add"))

        // Thumbnail deleted if generated
        assertTrue(thumbnailGenerator.deletedThumbnails.isNotEmpty())
    }

    @Test
    fun importer_onProgress_calledWith1NToNN_andDuplicatesExcludedFromN() = runTest(testDispatcher) {
        // Pre-populate an asset so one URI is considered a duplicate
        mediaRepo.addAsset(
            MediaAsset(
                id = "existing_asset",
                projectId = "p1",
                sourceUri = "content://media/dup",
                displayName = "dup.mp4",
                mimeType = "video/mp4",
                sizeBytes = 1024L,
                durationMs = 5000L,
                width = 1080,
                height = 1920,
                rotationDegrees = 0,
                addedAt = 1000L
            )
        )

        val uris = listOf(
            "content://media/dup",  // duplicate, excluded from N
            "content://media/v1",   // index 1 in toProcess
            "content://media/v2",   // index 2 in toProcess
            "content://media/v3"    // index 3 in toProcess
        )

        val progressCalls = mutableListOf<Pair<Int, Int>>()
        val result = importer.importVideos("p1", uris) { current, total ->
            progressCalls.add(current to total)
        }

        assertEquals(3, result.totalAttempted)
        assertEquals(3, result.importedCount)
        assertEquals(1, result.skippedDuplicates)

        // N should be 3 (duplicates excluded)
        val expectedCalls = listOf(1 to 3, 2 to 3, 3 to 3)
        assertEquals(expectedCalls, progressCalls)
    }

    // --- EditorViewModel Tests ---

    @Test
    fun editorViewModel_selectionDefaultsToFirstAsset_fallsBackCorrectlyOnRemoval() = runTest(testDispatcher) {
        projectRepo.setProject(Project(id = "p1", name = "Test", createdAt = 1000L, updatedAt = 1000L, aspectRatio = "9:16"))
        val asset1 = MediaAsset(
            id = "a1", projectId = "p1", sourceUri = "content://media/1", displayName = "v1.mp4",
            mimeType = "video/mp4", sizeBytes = 1024L, durationMs = 3000L, width = 1080, height = 1920,
            rotationDegrees = 0, addedAt = 1000L, availability = Availability.Available
        )
        val asset2 = MediaAsset(
            id = "a2", projectId = "p1", sourceUri = "content://media/2", displayName = "v2.mp4",
            mimeType = "video/mp4", sizeBytes = 1024L, durationMs = 3000L, width = 1080, height = 1920,
            rotationDegrees = 0, addedAt = 2000L, availability = Availability.Available
        )
        mediaRepo.addAsset(asset1)
        mediaRepo.addAsset(asset2)

        val vm = EditorViewModel("p1", projectRepo, mediaRepo, importer, availabilityChecker)
        val collectJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        // Selection defaults to first asset
        assertEquals("a1", vm.uiState.value.selectedAssetId)

        // Removing selected asset falls back to next
        vm.removeAsset("a1")
        advanceUntilIdle()

        assertEquals("a2", vm.uiState.value.selectedAssetId)

        collectJob.cancel()
    }

    @Test
    fun editorViewModel_importVideos_turnsImportProgressOnThenOff_andSetsOneSnackbarOnFailure_noneIfNothingFailed() = runTest(testDispatcher) {
        val testImporter = MediaImporter(
            videoMetadataReader = metadataReader,
            uriGrantManager = grantManager,
            thumbnailGenerator = thumbnailGenerator,
            mediaRepository = mediaRepo,
            projectRepository = projectRepo,
            dispatcher = StandardTestDispatcher(testScheduler)
        )
        projectRepo.setProject(Project(id = "p1", name = "Test", createdAt = 1000L, updatedAt = 1000L, aspectRatio = "9:16"))
        val vm = EditorViewModel("p1", projectRepo, mediaRepo, testImporter, availabilityChecker)

        val observedProgress = mutableListOf<ImportProgress?>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect { state ->
                observedProgress.add(state.importProgress)
            }
        }
        advanceUntilIdle()

        // Successful import -> no snackbar
        vm.importVideos(listOf("content://media/success1"))
        // Check while running or advance until finished
        advanceUntilIdle()

        assertNull(vm.uiState.value.importProgress)
        assertNull(vm.uiState.value.snackbarMessage)
        assertTrue("Expected to observe non-null importProgress at least once", observedProgress.any { it != null })

        // Import failure -> one snackbar message
        grantManager.persistSucceeds["content://media/fail1"] = false
        vm.importVideos(listOf("content://media/fail1"))
        advanceUntilIdle()

        assertNull(vm.uiState.value.importProgress)
        assertEquals("1 video couldn't be imported", vm.uiState.value.snackbarMessage)

        collectJob.cancel()
    }

    @Test
    fun editorViewModel_unavailableAssets_remainInAssetListWithAvailabilityUnavailable() = runTest(testDispatcher) {
        projectRepo.setProject(Project(id = "p1", name = "Test", createdAt = 1000L, updatedAt = 1000L, aspectRatio = "9:16"))
        val asset = MediaAsset(
            id = "a_unavail", projectId = "p1", sourceUri = "content://media/missing", displayName = "missing.mp4",
            mimeType = "video/mp4", sizeBytes = 1024L, durationMs = 3000L, width = 1080, height = 1920,
            rotationDegrees = 0, addedAt = 1000L
        )
        mediaRepo.addAsset(asset)
        availabilityChecker.availabilityMap["content://media/missing"] = false

        val vm = EditorViewModel("p1", projectRepo, mediaRepo, importer, availabilityChecker)
        val collectJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        val assets = vm.uiState.value.assets
        assertEquals(1, assets.size)
        assertEquals("a_unavail", assets[0].id)
        assertEquals(Availability.Unavailable, assets[0].availability)

        collectJob.cancel()
    }

    @Test
    fun editorViewModel_autoSelection_neverSelectsUnavailableUnlessAllUnavailable() = runTest(testDispatcher) {
        projectRepo.setProject(Project(id = "p1", name = "Test", createdAt = 1000L, updatedAt = 1000L, aspectRatio = "9:16"))
        val assetUnavail = MediaAsset(
            id = "a1_unavail", projectId = "p1", sourceUri = "content://media/unavail", displayName = "unavail.mp4",
            mimeType = "video/mp4", sizeBytes = 1024L, durationMs = 3000L, width = 1080, height = 1920,
            rotationDegrees = 0, addedAt = 1000L
        )
        val assetAvail = MediaAsset(
            id = "a2_avail", projectId = "p1", sourceUri = "content://media/avail", displayName = "avail.mp4",
            mimeType = "video/mp4", sizeBytes = 1024L, durationMs = 3000L, width = 1080, height = 1920,
            rotationDegrees = 0, addedAt = 2000L
        )
        mediaRepo.addAsset(assetUnavail)
        mediaRepo.addAsset(assetAvail)

        availabilityChecker.availabilityMap["content://media/unavail"] = false
        availabilityChecker.availabilityMap["content://media/avail"] = true

        val vm = EditorViewModel("p1", projectRepo, mediaRepo, importer, availabilityChecker)
        val collectJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        // Even though a1_unavail is first in list, auto-selection selects a2_avail!
        assertEquals("a2_avail", vm.uiState.value.selectedAssetId)

        collectJob.cancel()
    }

    @Test
    fun editorViewModel_autoSelection_selectsFirstIfAllUnavailable() = runTest(testDispatcher) {
        projectRepo.setProject(Project(id = "p1", name = "Test", createdAt = 1000L, updatedAt = 1000L, aspectRatio = "9:16"))
        val asset1 = MediaAsset(
            id = "a1_unavail", projectId = "p1", sourceUri = "content://media/unavail1", displayName = "unavail1.mp4",
            mimeType = "video/mp4", sizeBytes = 1024L, durationMs = 3000L, width = 1080, height = 1920,
            rotationDegrees = 0, addedAt = 1000L
        )
        val asset2 = MediaAsset(
            id = "a2_unavail", projectId = "p1", sourceUri = "content://media/unavail2", displayName = "unavail2.mp4",
            mimeType = "video/mp4", sizeBytes = 1024L, durationMs = 3000L, width = 1080, height = 1920,
            rotationDegrees = 0, addedAt = 2000L
        )
        mediaRepo.addAsset(asset1)
        mediaRepo.addAsset(asset2)

        availabilityChecker.availabilityMap["content://media/unavail1"] = false
        availabilityChecker.availabilityMap["content://media/unavail2"] = false

        val vm = EditorViewModel("p1", projectRepo, mediaRepo, importer, availabilityChecker)
        val collectJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        // When all unavailable, falls back to first
        assertEquals("a1_unavail", vm.uiState.value.selectedAssetId)

        collectJob.cancel()
    }

    @Test
    fun editorViewModel_unknownToUnavailableTransition() = runTest(testDispatcher) {
        projectRepo.setProject(Project(id = "p1", name = "Test", createdAt = 1000L, updatedAt = 1000L, aspectRatio = "9:16"))
        val asset = MediaAsset(
            id = "a1", projectId = "p1", sourceUri = "content://media/target", displayName = "test.mp4",
            mimeType = "video/mp4", sizeBytes = 1024L, durationMs = 3000L, width = 1080, height = 1920,
            rotationDegrees = 0, addedAt = 1000L, availability = Availability.Unknown
        )
        mediaRepo.addAsset(asset)
        availabilityChecker.availabilityMap["content://media/target"] = false

        val vm = EditorViewModel("p1", projectRepo, mediaRepo, importer, availabilityChecker)
        val collectJob = launch { vm.uiState.collect {} }

        // Initial state before availability check completes has asset with Availability.Unknown
        val initialAsset = vm.uiState.value.assets.firstOrNull()
        if (initialAsset != null) {
            assertEquals(Availability.Unknown, initialAsset.availability)
        }

        advanceUntilIdle()

        // After checkAssetsAvailability finishes, asset transitions to Availability.Unavailable
        val checkedAsset = vm.uiState.value.assets.first()
        assertEquals(Availability.Unavailable, checkedAsset.availability)

        collectJob.cancel()
    }

    @Test
    fun editorViewModel_loadingAndLoadedState_whenProjectExists() = runTest(testDispatcher) {
        projectRepo.setProject(Project(id = "p1", name = "Test", createdAt = 1000L, updatedAt = 1000L, aspectRatio = "9:16"))
        val vm = EditorViewModel("p1", projectRepo, mediaRepo, importer, availabilityChecker)

        // Initially before flow emissions or advance, isLoading is true
        assertTrue(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.projectNotFound)

        val collectJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        // After load completes
        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.projectNotFound)
        assertNotNull(vm.uiState.value.project)
        assertEquals("p1", vm.uiState.value.project?.id)

        collectJob.cancel()
    }

    @Test
    fun editorViewModel_projectNotFound_setsFlagWhenProjectDoesNotExist() = runTest(testDispatcher) {
        val vm = EditorViewModel("non_existent_id", projectRepo, mediaRepo, importer, availabilityChecker)
        val collectJob = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.projectNotFound)
        assertNull(vm.uiState.value.project)

        collectJob.cancel()
    }
}
