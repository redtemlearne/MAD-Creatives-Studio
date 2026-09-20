package com.example.model

data class Project(
    val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val aspectRatio: String = "9:16",
    val clipCount: Int = 0,
    val firstAssetId: String? = null,
    val firstAssetSourceUri: String? = null
)
