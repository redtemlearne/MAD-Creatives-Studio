package com.example.ui.editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.media.MediaImporter
import com.example.data.repository.MediaRepository
import com.example.data.repository.ProjectRepository
import com.example.model.MediaAsset
import com.example.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EditorUiState(
    val project: Project? = null,
    val isLoading: Boolean = true,
    val projectNotFound: Boolean = false,
    val assets: List<MediaAsset> = emptyList(),
    val selectedAssetId: String? = null,
    val isImporting: Boolean = false,
    val snackbarMessage: String? = null
) {
    val selectedAsset: MediaAsset?
        get() = assets.find { it.id == selectedAssetId }
}

class EditorViewModel(
    val projectId: String,
    private val projectRepository: ProjectRepository,
    private val mediaRepository: MediaRepository,
    private val context: Context
) : ViewModel() {

    private val mediaImporter = MediaImporter(context, mediaRepository)

    private val _selectedAssetId = MutableStateFlow<String?>(null)
    private val _availabilityMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val _isImporting = MutableStateFlow(false)
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
            val isAvail = availability[asset.id] ?: true
            asset.copy(isAvailable = isAvail)
        }
    }

    val uiState: StateFlow<EditorUiState> = combine(
        projectFlow,
        assetsFlow,
        _selectedAssetId,
        _isImporting,
        _snackbarMessage,
        _initialLoadDone
    ) { flows ->
        val project = flows[0] as Project?
        @Suppress("UNCHECKED_CAST")
        val assets = flows[1] as List<MediaAsset>
        val selectedId = flows[2] as String?
        val isImporting = flows[3] as Boolean
        val snackbarMsg = flows[4] as String?
        val initialLoadDone = flows[5] as Boolean

        // Auto-select first asset if none selected or if previously selected asset was removed
        val effectiveSelectedId = when {
            selectedId != null && assets.any { it.id == selectedId } -> selectedId
            assets.isNotEmpty() -> {
                val firstId = assets.first().id
                if (_selectedAssetId.value != firstId) {
                    _selectedAssetId.value = firstId
                }
                firstId
            }
            else -> {
                if (_selectedAssetId.value != null) {
                    _selectedAssetId.value = null
                }
                null
            }
        }

        val isLoading = !initialLoadDone && project == null
        val notFound = initialLoadDone && project == null

        EditorUiState(
            project = project,
            isLoading = isLoading,
            projectNotFound = notFound,
            assets = assets,
            selectedAssetId = effectiveSelectedId,
            isImporting = isImporting,
            snackbarMessage = snackbarMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EditorUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            // First check if project exists
            val proj = projectRepository.getProjectOnce(projectId)
            _initialLoadDone.value = true
            if (proj != null) {
                checkAssetsAvailability()
            }
        }
    }

    fun checkAssetsAvailability() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentAssets = mediaRepository.getAssetsOnce(projectId)
            val newAvailability = mutableMapOf<String, Boolean>()
            currentAssets.forEach { asset ->
                val available = try {
                    val uri = Uri.parse(asset.sourceUri)
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
                } catch (_: Exception) {
                    false
                }
                newAvailability[asset.id] = available
            }
            _availabilityMap.value = newAvailability
        }
    }

    fun selectAsset(assetId: String) {
        _selectedAssetId.value = assetId
    }

    fun importVideos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isImporting.value = true
            val result = mediaImporter.importVideos(projectId, uris)
            _isImporting.value = false

            // Report failures in ONE Snackbar ("2 of 3 videos couldn't be imported")
            if (result.failedCount > 0) {
                val message = if (result.totalAttempted == 1) {
                    "1 video couldn't be imported"
                } else {
                    "${result.failedCount} of ${result.totalAttempted} videos couldn't be imported"
                }
                _snackbarMessage.value = message
            }

            // Recheck availability and trigger auto-selection
            checkAssetsAvailability()
        }
    }

    fun removeAsset(assetId: String) {
        viewModelScope.launch {
            mediaRepository.removeAsset(assetId)
            val remaining = mediaRepository.getAssetsOnce(projectId)
            if (_selectedAssetId.value == assetId) {
                _selectedAssetId.value = remaining.firstOrNull()?.id
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
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditorViewModel(projectId, projectRepository, mediaRepository, context) as T
            }
        }
    }
}
