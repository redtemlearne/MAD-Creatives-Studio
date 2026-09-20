package com.example.model

data class MediaAsset(
    val id: String,
    val projectId: String,
    val sourceUri: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val addedAt: Long,
    val isAvailable: Boolean = true
)
