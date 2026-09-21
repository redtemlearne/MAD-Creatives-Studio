package com.example.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.media.MediaImporter
import com.example.data.media.UriAvailabilityChecker
import com.example.data.repository.MediaRepository
import com.example.data.repository.ProjectRepository
import com.example.model.Availability
import com.example.model.MediaAsset
import com.example.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ImportProgress(
    val current: Int,
    val total: Int
)

data class EditorUiState(
    val project: Project? = null,
    val isLoading: Boolean = true,
    val projectNotFound: Boolean = false,
    val assets: List<MediaAsset> = emptyList(),
    val selectedAssetId: String? = null,
    val importProgress: ImportProgress? = null,
    val snackbarMessage: String? = null
) {
    val isImporting: Boolean get() = importProgress != null
    val selectedAsset: MediaAsset?
        get() = assets.find { it.id == selectedAssetId }
}

class EditorViewModel(
    val projectId: String,
    private val projectRepository: ProjectRepository,
    private val mediaRepository: MediaRepository,
    private val mediaImporter: MediaImporter,
    private val uriAvailabilityChecker: UriAvailabilityChecker
) : ViewModel() {

    private val _selectedAssetId = MutableStateFlow<String?>(null)
    private val _availabilityMap = MutableStateFlow<Map<String, Availability>>(emptyMap())
    private val _importProgress = MutableStateFlow<ImportProgress?>(null)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _initialLoadDone = MutableStateFlow(false)

    // Flow for the project
    private val projectFlow = projectRepository.getProject(projectId)

    // Flow for assets merged with availability
    private val assetsFlow = combine(
        mediaRepository.getAssets(projectId),
        _availabilityMap
    ) { rawAssets, availability ->
        rawAssets.map { asset ->
            val avail = availability[asset.id] ?: Availability.Unknown
            asset.copy(availability = avail)
        }
    }

    val uiState: StateFlow<EditorUiState> = combine(
        projectFlow,
        assetsFlow,
        _selectedAssetId,
        _importProgress,
        _snackbarMessage,
        _initialLoadDone
    ) { flows ->
        val project = flows[0] as Project?
        @Suppress("UNCHECKED_CAST")
        val assets = flows[1] as List<MediaAsset>
        val selectedId = flows[2] as String?
        val importProg = flows[3] as ImportProgress?
        val snackbarMsg = flows[4] as String?
        val initialLoadDone = flows[5] as Boolean

        // Pure transform: no writes to _selectedAssetId inside combine
        // Auto-selection never selects an unavailable asset unless all assets are unavailable
        val effectiveSelectedId = when {
            selectedId != null && assets.any { it.id == selectedId && it.isAvailable } -> selectedId
            selectedId != null && assets.any { it.id == selectedId } && assets.none { it.isAvailable } -> selectedId
            else -> assets.firstOrNull { it.isAvailable }?.id ?: assets.firstOrNull()?.id
        }

        val isLoading = !initialLoadDone && project == null
        val notFound = initialLoadDone && project == null

        EditorUiState(
            project = project,
            isLoading = isLoading,
            projectNotFound = notFound,
            assets = assets,
            selectedAssetId = effectiveSelectedId,
            importProgress = importProg,
            snackbarMessage = snackbarMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EditorUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            val proj = projectRepository.getProjectOnce(projectId)
            _initialLoadDone.value = true
            if (proj != null) {
                checkAssetsAvailability()
            }
        }
    }

    fun checkAssetsAvailability() {
        viewModelScope.launch {
            val currentAssets = mediaRepository.getAssetsOnce(projectId)
            val newAvailability = mutableMapOf<String, Availability>()
            currentAssets.forEach { asset ->
                val isAvail = uriAvailabilityChecker.isAvailable(asset.sourceUri)
                newAvailability[asset.id] = if (isAvail) Availability.Available else Availability.Unavailable
            }
            _availabilityMap.value = newAvailability
        }
    }

    fun selectAsset(assetId: String) {
        _selectedAssetId.value = assetId
    }

    fun importVideos(uris: List<String>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            try {
                _importProgress.value = ImportProgress(1, uris.size)
                val result = mediaImporter.importVideos(
                    projectId = projectId,
                    uris = uris,
                    onProgress = { current, total ->
                        _importProgress.value = ImportProgress(current, total)
                    }
                )

                // Report failures in ONE Snackbar ("2 of 3 videos couldn't be imported")
                if (result.failedCount > 0) {
                    val message = if (result.totalAttempted == 1) {
                        "1 video couldn't be imported"
                    } else {
                        "${result.failedCount} of ${result.totalAttempted} videos couldn't be imported"
                    }
                    _snackbarMessage.value = message
                }

                checkAssetsAvailability()
            } finally {
                _importProgress.value = null
            }
        }
    }

    fun removeAsset(assetId: String) {
        viewModelScope.launch {
            val currentAssets = mediaRepository.getAssetsOnce(projectId)
            val currentIndex = currentAssets.indexOfFirst { it.id == assetId }
            mediaRepository.removeAsset(assetId)
            if (_selectedAssetId.value == assetId) {
                val remaining = mediaRepository.getAssetsOnce(projectId)
                val nextAsset = if (currentIndex in remaining.indices) {
                    remaining[currentIndex]
                } else {
                    remaining.lastOrNull()
                }
                _selectedAssetId.value = nextAsset?.id
            }
        }
    }

    fun renameProject(newName: String) {
        viewModelScope.launch {
            projectRepository.renameProject(projectId, newName)
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    companion object {
        fun provideFactory(
            projectId: String,
            projectRepository: ProjectRepository,
            mediaRepository: MediaRepository,
            mediaImporter: MediaImporter,
            uriAvailabilityChecker: UriAvailabilityChecker
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditorViewModel(
                    projectId = projectId,
                    projectRepository = projectRepository,
                    mediaRepository = mediaRepository,
                    mediaImporter = mediaImporter,
                    uriAvailabilityChecker = uriAvailabilityChecker
                ) as T
            }
        }
    }
}
