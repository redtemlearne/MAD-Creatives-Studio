package com.example.data.local

data class ProjectWithStats(
    val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val aspectRatio: String,
    val clipCount: Int,
    val firstAssetId: String?,
    val firstAssetSourceUri: String?
)
