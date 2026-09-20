package com.example.di

import android.content.Context
import com.example.data.local.StudioDatabase
import com.example.data.media.AndroidThumbnailGenerator
import com.example.data.media.AndroidUriAvailabilityChecker
import com.example.data.media.AndroidUriGrantManager
import com.example.data.media.AndroidVideoMetadataReader
import com.example.data.media.MediaImporter
import com.example.data.media.ThumbnailGenerator
import com.example.data.media.UriAvailabilityChecker
import com.example.data.media.UriGrantManager
import com.example.data.media.VideoMetadataReader
import com.example.data.repository.MediaRepository
import com.example.data.repository.ProjectRepository
import com.example.data.repository.RoomMediaRepository
import com.example.data.repository.RoomProjectRepository

interface AppContainer {
    val projectRepository: ProjectRepository
    val mediaRepository: MediaRepository
    val videoMetadataReader: VideoMetadataReader
    val uriGrantManager: UriGrantManager
    val thumbnailGenerator: ThumbnailGenerator
    val uriAvailabilityChecker: UriAvailabilityChecker
    val mediaImporter: MediaImporter
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

    override val videoMetadataReader: VideoMetadataReader by lazy {
        AndroidVideoMetadataReader(context)
    }

    override val uriGrantManager: UriGrantManager by lazy {
        AndroidUriGrantManager(context)
    }

    override val thumbnailGenerator: ThumbnailGenerator by lazy {
        AndroidThumbnailGenerator(context)
    }

    override val uriAvailabilityChecker: UriAvailabilityChecker by lazy {
        AndroidUriAvailabilityChecker(context)
    }

    override val mediaImporter: MediaImporter by lazy {
        MediaImporter(
            videoMetadataReader = videoMetadataReader,
            uriGrantManager = uriGrantManager,
            thumbnailGenerator = thumbnailGenerator,
            mediaRepository = mediaRepository,
            projectRepository = projectRepository
        )
    }
}
