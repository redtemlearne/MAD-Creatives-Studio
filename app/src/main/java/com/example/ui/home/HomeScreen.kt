package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.Project
import com.example.ui.components.ConfirmDeleteProjectBottomSheet
import com.example.ui.components.NewProjectBottomSheet
import com.example.ui.components.ProjectOptionsBottomSheet
import com.example.ui.components.StudioIconButton
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioOnAccent
import com.example.ui.theme.StudioRadius
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTypography
import com.example.util.LocalWindowWidthSizeClass
import com.example.util.isWide

@Composable
fun HomeScreen(
    projects: List<Project>,
    onOpenProject: (projectId: String) -> Unit,
    onCreateProject: (projectName: String) -> Unit,
    onDeleteProject: (projectId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showNewProjectSheet by remember { mutableStateOf(false) }
    var projectForOptions by remember { mutableStateOf<Project?>(null) }
    var projectToConfirmDelete by remember { mutableStateOf<Project?>(null) }
    val isWideLayout = LocalWindowWidthSizeClass.current.isWide()

    if (showNewProjectSheet) {
        NewProjectBottomSheet(
            onDismiss = { showNewProjectSheet = false },
            onCreateProject = { name ->
                showNewProjectSheet = false
                onCreateProject(name)
            }
        )
    }

    projectForOptions?.let { project ->
        ProjectOptionsBottomSheet(
            projectName = project.name,
            onDismiss = { projectForOptions = null },
            onDeleteClick = {
                projectForOptions = null
                projectToConfirmDelete = project
            }
        )
    }

    projectToConfirmDelete?.let { project ->
        ConfirmDeleteProjectBottomSheet(
            projectName = project.name,
            onDismiss = { projectToConfirmDelete = null },
            onConfirmDelete = {
                onDeleteProject(project.id)
                projectToConfirmDelete = null
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground),
        containerColor = StudioBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            if (projects.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showNewProjectSheet = true },
                    containerColor = StudioAccent,
                    contentColor = StudioOnAccent,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    shape = RoundedCornerShape(StudioRadius.md),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("new_project_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Project",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val contentMaxWidth = if (isWideLayout) 840.dp else 600.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = contentMaxWidth)
                    .align(Alignment.TopCenter)
            ) {
                // Top Branding Area
                HomeTopBar()

                // Main Content Area
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = StudioSpacing.lg),
                    contentPadding = PaddingValues(
                        top = StudioSpacing.md,
                        bottom = StudioSpacing.xxxl + StudioSpacing.xxl
                    ),
                    verticalArrangement = Arrangement.spacedBy(StudioSpacing.md)
                ) {
                    item {
                        // Section Header with Title
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = StudioSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "My Projects",
                                    style = StudioTypography.headlineSmall,
                                    color = StudioTextPrimary
                                )
                                Text(
                                    text = if (projects.isEmpty()) "0 projects" else "${projects.size} ${if (projects.size == 1) "project" else "projects"}",
                                    style = StudioTypography.labelSmall,
                                    color = StudioTextMuted
                                )
                            }
                        }
                    }

                    if (projects.isEmpty()) {
                        item {
                            EmptyProjectsState(
                                onNewProjectClick = { showNewProjectSheet = true },
                                modifier = Modifier
                                    .padding(top = StudioSpacing.lg)
                                    .testTag("empty_projects_state")
                            )
                        }
                    } else {
                        items(
                            items = projects,
                            key = { it.id }
                        ) { project ->
                            ProjectCard(
                                project = project,
                                onClick = { onOpenProject(project.id) },
                                onLongClick = { projectForOptions = project }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = StudioSpacing.lg, vertical = StudioSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StudioSpacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(StudioRadius.sm))
                    .background(StudioAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = StudioOnAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "MAD Creatives Studio",
                style = StudioTypography.titleMedium,
                color = StudioTextPrimary
            )
        }

        // Settings icon button
        StudioIconButton(
            icon = Icons.Default.Settings,
            contentDescription = "Settings",
            onClick = { /* Settings action */ },
            tint = StudioTextSecondary,
            modifier = Modifier.testTag("home_settings_button")
        )
    }
}
