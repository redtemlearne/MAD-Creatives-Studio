package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.Project
import com.example.ui.components.Phase1NoticeDialog
import com.example.ui.components.RenameProjectDialog
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioSpacing

@Composable
fun EditorScreen(
    project: Project,
    onBackClick: () -> Unit,
    onRenameProject: (newName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showPhase1NoticeDialog by remember { mutableStateOf(false) }

    if (showRenameDialog) {
        RenameProjectDialog(
            currentName = project.name,
            onDismiss = { showRenameDialog = false },
            onRenameConfirm = { newName ->
                showRenameDialog = false
                onRenameProject(newName)
            }
        )
    }

    if (showPhase1NoticeDialog) {
        Phase1NoticeDialog(
            onDismiss = { showPhase1NoticeDialog = false }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground),
        containerColor = StudioBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            EditorTopBar(
                projectName = project.name,
                onBackClick = onBackClick,
                onRenameClick = { showRenameDialog = true }
            )
        },
        bottomBar = {
            // In wide mode, we place bottom bar in right column; in standard mode, we keep bottomBar here
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideLayout = maxWidth >= 600.dp

            if (isWideLayout) {
                // Wide / Landscape Layout: Preview on Left, Timeline on Right
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(StudioSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(StudioSpacing.md)
                ) {
                    // Left Column: Video Preview + Playback Bar
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        VideoPreviewArea(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(StudioSpacing.sm))
                        PlaybackBar(modifier = Modifier.fillMaxWidth())
                    }

                    // Right Column: Timeline Empty Area + Bottom Controls
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        TimelineEmptyState(
                            onAddMediaClick = { showPhase1NoticeDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(StudioSpacing.sm))

                        EditorBottomBar(
                            onAddMediaClick = { showPhase1NoticeDialog = true },
                            onMoreClick = { showPhase1NoticeDialog = true }
                        )
                    }
                }
            } else {
                // Compact Phone Portrait Layout: Stacked Preview -> Playback -> Timeline -> Bottom Bar
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
                        VideoPreviewArea(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(StudioSpacing.xs))
                        PlaybackBar(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(StudioSpacing.xs))
                        TimelineEmptyState(
                            onAddMediaClick = { showPhase1NoticeDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Spacer(modifier = Modifier.height(StudioSpacing.sm))
                    }

                    EditorBottomBar(
                        onAddMediaClick = { showPhase1NoticeDialog = true },
                        onMoreClick = { showPhase1NoticeDialog = true }
                    )
                }
            }
        }
    }
}
