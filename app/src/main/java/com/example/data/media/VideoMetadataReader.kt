package com.example.data.media

data class VideoMetadata(
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int
)

interface VideoMetadataReader {
    fun read(uri: String): VideoMetadata?
}
