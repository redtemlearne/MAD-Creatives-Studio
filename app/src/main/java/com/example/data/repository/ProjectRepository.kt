package com.example.data.repository

import com.example.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getProjects(): Flow<List<Project>>
    fun getProject(id: String): Flow<Project?>
    suspend fun getProjectOnce(id: String): Project?
    suspend fun createProject(name: String): Project
    suspend fun renameProject(id: String, newName: String)
    suspend fun updateProjectTimestamp(id: String, updatedAt: Long)
    suspend fun deleteProject(id: String)
}
