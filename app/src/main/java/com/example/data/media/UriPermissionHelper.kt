package com.example.data.media

import android.content.Context
import android.content.Intent
import android.net.Uri

object UriPermissionHelper {
    fun releaseGrantIfUnused(context: Context, uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
            // Ignored if grant wasn't held or already released
        }
    }
}
