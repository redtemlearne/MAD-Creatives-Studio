package com.example.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.ui.components.RenameProjectBottomSheet
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTypography
import com.example.util.LocalWindowWidthSizeClass
import com.example.util.isWide

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isWideLayout = LocalWindowWidthSizeClass.current.isWide()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRenameSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Media file picker launcher
    val openMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importVideos(uris)
        }
    }

    // Handle project not found -> navigate back to Home
    LaunchedEffect(uiState.projectNotFound) {
        if (uiState.projectNotFound) {
            onBackClick()
        }
    }

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Loading State for navigation restore
    if (uiState.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(StudioBackground)
                .testTag("editor_loading_container"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = StudioAccent,
                    modifier = Modifier.testTag("editor_loading_indicator")
                )
                Spacer(modifier = Modifier.height(StudioSpacing.md))
                Text(
                    text = "Loading project...",
                    style = StudioTypography.bodyMedium,
                    color = StudioTextMuted
                )
            }
        }
        return
    }

    val project = uiState.project ?: return

    // One ExoPlayer per editor screen, released on dispose, paused on ON_STOP
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = false
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Observe lifecycle for pause on STOP and availability check on START
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    viewModel.checkAssetsAvailability()
                }
                Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var playerError by remember { mutableStateOf<String?>(null) }
    var playbackEnded by remember { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                playbackEnded = (playbackState == Player.STATE_ENDED)
                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                playerError = "Cannot play this video"
                isPlaying = false
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Load selected asset into player paused at 0
    val selectedAsset = uiState.selectedAsset
    LaunchedEffect(selectedAsset?.id) {
        playerError = null
        playbackEnded = false
        if (selectedAsset != null && selectedAsset.isAvailable) {
            try {
                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(selectedAsset.sourceUri)))
                exoPlayer.seekTo(0)
                exoPlayer.pause()
                exoPlayer.prepare()
            } catch (_: Exception) {
                playerError = "Cannot load video"
            }
        } else {
            exoPlayer.clearMediaItems()
        }
    }

    val canPlay = selectedAsset != null && selectedAsset.isAvailable && playerError == null
    val onTogglePlay = {
        if (canPlay) {
            if (playbackEnded) {
                exoPlayer.seekTo(0)
                exoPlayer.play()
            } else if (isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
        }
    }

    if (showRenameSheet) {
        RenameProjectBottomSheet(
            currentName = project.name,
            onDismiss = { showRenameSheet = false },
            onRenameConfirm = { newName ->
                showRenameSheet = false
                viewModel.renameProject(newName)
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground),
        containerColor = StudioBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.testTag("editor_snackbar_host")
            )
        },
        topBar = {
            EditorTopBar(
                projectName = project.name,
                onBackClick = onBackClick,
                onRenameClick = { showRenameSheet = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isWideLayout) {
                // Wide / Tablet Layout: Preview on Left, Media Tray / Empty State on Right
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(StudioSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(StudioSpacing.md)
                ) {
                    // Left Column: 9:16 Video Preview + Playback Bar
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        VideoPreviewArea(
                            player = exoPlayer,
                            selectedAsset = selectedAsset,
                            hasAssets = uiState.assets.isNotEmpty(),
                            errorMessage = playerError,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxHeight(0.85f)
                        )
                        Spacer(modifier = Modifier.height(StudioSpacing.sm))
                        PlaybackBar(
                            player = exoPlayer,
                            isPlaying = isPlaying,
                            durationMs = selectedAsset?.durationMs ?: 0L,
                            enabled = canPlay,
                            onTogglePlay = onTogglePlay,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Right Column: Media Tray or Empty State + Bottom Controls
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (uiState.assets.isEmpty()) {
                            TimelineEmptyState(
                                onAddMediaClick = { openMediaLauncher.launch(arrayOf("video/*")) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            )
                        } else {
                            MediaTray(
                                assets = uiState.assets,
                                selectedAssetId = uiState.selectedAssetId,
                                onSelectAsset = { viewModel.selectAsset(it) },
                                onRemoveAsset = { viewModel.removeAsset(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = StudioSpacing.md)
                            )
                        }

                        Spacer(modifier = Modifier.height(StudioSpacing.sm))

                        EditorBottomBar(
                            onAddMediaClick = { openMediaLauncher.launch(arrayOf("video/*")) },
                            onMoreClick = { showRenameSheet = true }
                        )
                    }
                }
            } else {
                // Compact Phone Portrait Layout: Stacked Preview -> Playback -> Media Tray/Empty State -> Bottom Bar
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(StudioSpacing.xs))
                        VideoPreviewArea(
                            player = exoPlayer,
                            selectedAsset = selectedAsset,
                            hasAssets = uiState.assets.isNotEmpty(),
                            errorMessage = playerError,
                            modifier = Modifier
                                .weight(1.2f)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(StudioSpacing.xs))
                        PlaybackBar(
                            player = exoPlayer,
                            isPlaying = isPlaying,
                            durationMs = selectedAsset?.durationMs ?: 0L,
                            enabled = canPlay,
                            onTogglePlay = onTogglePlay,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(StudioSpacing.xs))

                        if (uiState.assets.isEmpty()) {
                            TimelineEmptyState(
                                onAddMediaClick = { openMediaLauncher.launch(arrayOf("video/*")) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        } else {
                            MediaTray(
                                assets = uiState.assets,
                                selectedAssetId = uiState.selectedAssetId,
                                onSelectAsset = { viewModel.selectAsset(it) },
                                onRemoveAsset = { viewModel.removeAsset(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = StudioSpacing.lg)
                            )
                        }
                        Spacer(modifier = Modifier.height(StudioSpacing.sm))
                    }

                    EditorBottomBar(
                        onAddMediaClick = { openMediaLauncher.launch(arrayOf("video/*")) },
                        onMoreClick = { showRenameSheet = true }
                    )
                }
            }
        }
    }
}
