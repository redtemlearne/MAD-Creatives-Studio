package com.example.data.media

interface UriGrantManager {
    fun persist(uri: String): Boolean
    fun release(uri: String)
}
