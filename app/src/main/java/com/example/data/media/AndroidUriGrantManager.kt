package com.example.data.media

import android.content.Context
import android.content.Intent
import android.net.Uri

class AndroidUriGrantManager(
    private val context: Context
) : UriGrantManager {
    override fun persist(uri: String): Boolean {
        return try {
            val parsedUri = Uri.parse(uri)
            context.contentResolver.takePersistableUriPermission(
                parsedUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun release(uri: String) {
        try {
            val parsedUri = Uri.parse(uri)
            context.contentResolver.releasePersistableUriPermission(
                parsedUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {}
    }
}
