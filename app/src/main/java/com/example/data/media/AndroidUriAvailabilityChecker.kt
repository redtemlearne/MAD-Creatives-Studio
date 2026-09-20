package com.example.data.media

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidUriAvailabilityChecker(
    private val context: Context
) : UriAvailabilityChecker {
    override suspend fun isAvailable(uri: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val parsedUri = Uri.parse(uri)
            context.contentResolver.openFileDescriptor(parsedUri, "r")?.use { true } ?: false
        } catch (_: Exception) {
            false
        }
    }
}
