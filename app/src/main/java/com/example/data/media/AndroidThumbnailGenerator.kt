package com.example.data.media

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

class AndroidThumbnailGenerator(
    private val context: Context
) : ThumbnailGenerator {
    override fun generate(assetId: String, uri: String): Boolean {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(uri))
            val thumbFile = ThumbnailManager.getThumbnailFile(context, assetId)
            val bitmap = ThumbnailManager.extractAndSaveThumbnail(retriever, thumbFile)
            bitmap != null
        } catch (_: Exception) {
            false
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }
}
