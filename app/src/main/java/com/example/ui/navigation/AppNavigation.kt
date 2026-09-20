package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.di.AppContainer
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel

@Composable
fun AppNavigation(
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    var currentScreenRoute by rememberSaveable { mutableStateOf("home") }

    val currentScreen: Screen = if (currentScreenRoute.startsWith("editor:")) {
        Screen.Editor(currentScreenRoute.removePrefix("editor:"))
    } else {
        Screen.Home
    }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(appContainer.projectRepository)
    )
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

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
                    projects = homeUiState.projects,
                    isLoading = homeUiState.isLoading,
                    onOpenProject = { projectId ->
                        currentScreenRoute = "editor:$projectId"
                    },
                    onCreateProject = { projectName ->
                        homeViewModel.createProject(projectName) { newProjectId ->
                            currentScreenRoute = "editor:$newProjectId"
                        }
                    },
                    onDeleteProject = { projectId ->
                        homeViewModel.deleteProject(projectId)
                    }
                )
            }

            is Screen.Editor -> {
                BackHandler {
                    currentScreenRoute = "home"
                }

                val editorViewModel: EditorViewModel = viewModel(
                    key = "editor_${screen.projectId}",
                    factory = EditorViewModel.provideFactory(
                        projectId = screen.projectId,
                        projectRepository = appContainer.projectRepository,
                        mediaRepository = appContainer.mediaRepository,
                        mediaImporter = appContainer.mediaImporter,
                        uriAvailabilityChecker = appContainer.uriAvailabilityChecker
                    )
                )

                EditorScreen(
                    viewModel = editorViewModel,
                    onBackClick = {
                        currentScreenRoute = "home"
                    }
                )
            }
        }
    }
}
