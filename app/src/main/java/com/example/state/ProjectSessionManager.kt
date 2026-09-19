package com.example.state

import androidx.lifecycle.ViewModel
import com.example.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProjectSessionManager : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _activeProject = MutableStateFlow<Project?>(null)
    val activeProject: StateFlow<Project?> = _activeProject.asStateFlow()

    fun createProject(name: String): Project {
        val trimmed = name.trim().ifEmpty { "Untitled Project" }
        val newProject = Project(
            name = trimmed,
            lastEditedFormatted = "Just now",
            durationPlaceholder = "0:00"
        )
        _projects.value = listOf(newProject) + _projects.value
        _activeProject.value = newProject
        return newProject
    }

    fun openProject(id: String) {
        val found = _projects.value.find { it.id == id }
        _activeProject.value = found
    }

    fun closeProject() {
        _activeProject.value = null
    }

    fun renameProject(id: String, newName: String) {
        val trimmed = newName.trim().ifEmpty { "Untitled Project" }
        val nowFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        _projects.value = _projects.value.map { project ->
            if (project.id == id) {
                project.copy(
                    name = trimmed,
                    lastEditedFormatted = "Edited $nowFormatted"
                )
            } else {
                project
            }
        }
        if (_activeProject.value?.id == id) {
            _activeProject.value = _activeProject.value?.copy(
                name = trimmed,
                lastEditedFormatted = "Edited $nowFormatted"
            )
        }
    }

    fun getProject(id: String): Project? {
        return _projects.value.find { it.id == id }
    }
}
