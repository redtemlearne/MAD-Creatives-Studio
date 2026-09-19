package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.state.ProjectSessionManager
import com.example.ui.editor.EditorScreen
import com.example.ui.home.HomeScreen

@Composable
fun AppNavigation(
    sessionManager: ProjectSessionManager,
    modifier: Modifier = Modifier
) {
    var currentScreenRoute by rememberSaveable { mutableStateOf("home") }

    val currentScreen: Screen = if (currentScreenRoute.startsWith("editor:")) {
        Screen.Editor(currentScreenRoute.removePrefix("editor:"))
    } else {
        Screen.Home
    }

    val projects by sessionManager.projects.collectAsState()
    val activeProject by sessionManager.activeProject.collectAsState()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "AppScreenTransition",
        modifier = modifier
    ) { screen ->
        when (screen) {
            is Screen.Home -> {
                HomeScreen(
                    projects = projects,
                    onOpenProject = { projectId ->
                        sessionManager.openProject(projectId)
                        currentScreenRoute = "editor:$projectId"
                    },
                    onCreateProject = { projectName ->
                        val newProject = sessionManager.createProject(projectName)
                        currentScreenRoute = "editor:${newProject.id}"
                    }
                )
            }

            is Screen.Editor -> {
                BackHandler {
                    sessionManager.closeProject()
                    currentScreenRoute = "home"
                }

                // If active project is somehow null, fallback to Home
                val projectToDisplay = activeProject ?: sessionManager.getProject(screen.projectId)

                if (projectToDisplay != null) {
                    EditorScreen(
                        project = projectToDisplay,
                        onBackClick = {
                            sessionManager.closeProject()
                            currentScreenRoute = "home"
                        },
                        onRenameProject = { newName ->
                            sessionManager.renameProject(projectToDisplay.id, newName)
                        }
                    )
                } else {
                    HomeScreen(
                        projects = projects,
                        onOpenProject = { projectId ->
                            sessionManager.openProject(projectId)
                            currentScreenRoute = "editor:$projectId"
                        },
                        onCreateProject = { projectName ->
                            val newProject = sessionManager.createProject(projectName)
                            currentScreenRoute = "editor:${newProject.id}"
                        }
                    )
                }
            }
        }
    }
}
