package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.state.ProjectSessionManager
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MADCreativesTheme
import com.example.util.LocalWindowWidthSizeClass
import com.example.util.rememberWindowWidthSizeClass

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MADCreativesStudioApp()
        }
    }
}

@Composable
fun MADCreativesStudioApp(
    sessionManager: ProjectSessionManager = viewModel(),
    windowWidthSizeClass: WindowWidthSizeClass = rememberWindowWidthSizeClass()
) {
    CompositionLocalProvider(LocalWindowWidthSizeClass provides windowWidthSizeClass) {
        MADCreativesTheme {
            AppNavigation(
                sessionManager = sessionManager,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
