package com.example.ui.navigation

sealed class Screen {
    data object Home : Screen()
    data class Editor(val projectId: String) : Screen()
}
