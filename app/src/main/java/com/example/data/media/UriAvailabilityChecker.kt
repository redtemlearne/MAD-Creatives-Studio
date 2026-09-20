package com.example.data.media

interface UriAvailabilityChecker {
    suspend fun isAvailable(uri: String): Boolean
}
