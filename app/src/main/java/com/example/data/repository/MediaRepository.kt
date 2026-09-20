package com.example.data.repository

import com.example.model.MediaAsset
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getAssets(projectId: String): Flow<List<MediaAsset>>
    suspend fun getAssetsOnce(projectId: String): List<MediaAsset>
    suspend fun addAsset(asset: MediaAsset)
    suspend fun removeAsset(id: String)
    suspend fun countUriUsage(uri: String): Int
}
