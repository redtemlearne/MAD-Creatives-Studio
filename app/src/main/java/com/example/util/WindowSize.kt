package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

val LocalWindowWidthSizeClass = staticCompositionLocalOf { WindowWidthSizeClass.Compact }

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun rememberWindowWidthSizeClass(): WindowWidthSizeClass {
    val context = LocalContext.current
    val activity = context.findActivity()
    return if (activity != null) {
        calculateWindowSizeClass(activity).widthSizeClass
    } else {
        val config = LocalConfiguration.current
        when {
            config.screenWidthDp < 600 -> WindowWidthSizeClass.Compact
            config.screenWidthDp < 840 -> WindowWidthSizeClass.Medium
            else -> WindowWidthSizeClass.Expanded
        }
    }
}

fun WindowWidthSizeClass.isWide(): Boolean = this != WindowWidthSizeClass.Compact
