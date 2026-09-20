package com.example.di

import android.content.Context
import com.example.data.local.StudioDatabase
import com.example.data.repository.MediaRepository
import com.example.data.repository.ProjectRepository
import com.example.data.repository.RoomMediaRepository
import com.example.data.repository.RoomProjectRepository

interface AppContainer {
    val projectRepository: ProjectRepository
    val mediaRepository: MediaRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: StudioDatabase by lazy {
        StudioDatabase.getInstance(context)
    }

    override val projectRepository: ProjectRepository by lazy {
        RoomProjectRepository(database.projectDao(), database.mediaAssetDao(), context)
    }

    override val mediaRepository: MediaRepository by lazy {
        RoomMediaRepository(database.mediaAssetDao(), database.projectDao(), context)
    }
}
