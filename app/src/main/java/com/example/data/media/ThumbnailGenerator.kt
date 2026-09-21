package com.example.data.media

interface ThumbnailGenerator {
    fun generate(assetId: String, uri: String): Boolean
    fun deleteThumbnail(assetId: String): Boolean = false
}
