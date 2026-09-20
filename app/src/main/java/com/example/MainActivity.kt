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
import androidx.compose.ui.platform.LocalContext
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MADCreativesTheme
import com.example.util.LocalWindowWidthSizeClass
import com.example.util.rememberWindowWidthSizeClass

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as? MADCreativesApp)?.container
            ?: DefaultAppContainer(applicationContext)

        setContent {
            MADCreativesStudioApp(appContainer = appContainer)
        }
    }
}

@Composable
fun MADCreativesStudioApp(
    appContainer: AppContainer = (LocalContext.current.applicationContext as? MADCreativesApp)?.container
        ?: DefaultAppContainer(LocalContext.current.applicationContext),
    windowWidthSizeClass: WindowWidthSizeClass = rememberWindowWidthSizeClass()
) {
    CompositionLocalProvider(LocalWindowWidthSizeClass provides windowWidthSizeClass) {
        MADCreativesTheme {
            AppNavigation(
                appContainer = appContainer,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
