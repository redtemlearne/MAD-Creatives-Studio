package com.example.ui.editor

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
import com.example.ui.components.Phase1NoticeBottomSheet
import com.example.ui.components.RenameProjectBottomSheet
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioSpacing
import com.example.util.LocalWindowWidthSizeClass
import com.example.util.isWide

@Composable
fun EditorScreen(
    project: Project,
    onBackClick: () -> Unit,
    onRenameProject: (newName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRenameSheet by remember { mutableStateOf(false) }
    var showPhase1NoticeSheet by remember { mutableStateOf(false) }
    val isWideLayout = LocalWindowWidthSizeClass.current.isWide()

    if (showRenameSheet) {
        RenameProjectBottomSheet(
            currentName = project.name,
            onDismiss = { showRenameSheet = false },
            onRenameConfirm = { newName ->
                showRenameSheet = false
                onRenameProject(newName)
            }
        )
    }

    if (showPhase1NoticeSheet) {
        Phase1NoticeBottomSheet(
            onDismiss = { showPhase1NoticeSheet = false }
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
                // Wide / Tablet Layout: Preview on Left, Timeline & Controls on Right
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
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxHeight(0.85f)
                        )
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
                            onAddMediaClick = { showPhase1NoticeSheet = true },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(StudioSpacing.sm))

                        EditorBottomBar(
                            onAddMediaClick = { showPhase1NoticeSheet = true },
                            onMoreClick = { showPhase1NoticeSheet = true }
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
                        VideoPreviewArea(
                            modifier = Modifier
                                .weight(1.2f)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(StudioSpacing.xs))
                        PlaybackBar(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(StudioSpacing.xs))
                        TimelineEmptyState(
                            onAddMediaClick = { showPhase1NoticeSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Spacer(modifier = Modifier.height(StudioSpacing.sm))
                    }

                    EditorBottomBar(
                        onAddMediaClick = { showPhase1NoticeSheet = true },
                        onMoreClick = { showPhase1NoticeSheet = true }
                    )
                }
            }
        }
    }
}
