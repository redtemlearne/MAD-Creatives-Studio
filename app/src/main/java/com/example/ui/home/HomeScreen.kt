package com.example.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ScreenRotation
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.Project
import com.example.ui.components.NewProjectDialog
import com.example.ui.components.StudioAboutDialog
import com.example.ui.components.StudioIconButton
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioAccentMuted
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioOnAccent
import com.example.ui.theme.StudioRadius
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioSurfacePrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTypography
import com.example.util.findActivity
import com.example.util.toggleScreenOrientation

@Composable
fun HomeScreen(
    projects: List<Project>,
    onOpenProject: (projectId: String) -> Unit,
    onCreateProject: (projectName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onCreateProject = { name ->
                showNewProjectDialog = false
                onCreateProject(name)
            }
        )
    }

    if (showAboutDialog) {
        StudioAboutDialog(
            onDismiss = { showAboutDialog = false }
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
                    onClick = { showNewProjectDialog = true },
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val contentMaxWidth = 720.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = contentMaxWidth)
                    .align(Alignment.TopCenter)
            ) {
                // Top Branding Area
                HomeTopBar(
                    onAboutClick = { showAboutDialog = true }
                )

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
                        // Section Header with Title and Primary Action
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

                            if (projects.isNotEmpty()) {
                                StudioPrimaryButton(
                                    text = "New Project",
                                    icon = Icons.Default.Add,
                                    onClick = { showNewProjectDialog = true },
                                    modifier = Modifier.testTag("header_new_project_button")
                                )
                            }
                        }
                    }

                    if (projects.isEmpty()) {
                        item {
                            EmptyProjectsState(
                                onNewProjectClick = { showNewProjectDialog = true },
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
                                onClick = { onOpenProject(project.id) }
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
    onAboutClick: () -> Unit,
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StudioSpacing.xs)
        ) {
            val context = LocalContext.current
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

            StudioIconButton(
                icon = Icons.Default.ScreenRotation,
                contentDescription = if (isLandscape) "Switch to portrait" else "Switch to landscape",
                onClick = {
                    toggleScreenOrientation(context.findActivity())
                },
                tint = StudioTextSecondary,
                modifier = Modifier.testTag("home_rotate_button")
            )

            StudioIconButton(
                icon = Icons.Default.Info,
                contentDescription = "About MAD Creatives Studio",
                onClick = onAboutClick,
                tint = StudioTextSecondary,
                modifier = Modifier.testTag("home_about_button")
            )
        }
    }
}
