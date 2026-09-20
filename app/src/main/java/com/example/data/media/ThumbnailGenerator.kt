package com.example.data.media

interface ThumbnailGenerator {
    fun generate(assetId: String, uri: String): Boolean
}
