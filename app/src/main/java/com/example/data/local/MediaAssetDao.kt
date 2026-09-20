package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaAssetDao {

    @Query("SELECT * FROM media_assets WHERE projectId = :projectId ORDER BY addedAt ASC")
    fun getAssetsForProject(projectId: String): Flow<List<MediaAssetEntity>>

    @Query("SELECT * FROM media_assets WHERE projectId = :projectId ORDER BY addedAt ASC")
    suspend fun getAssetsForProjectOnce(projectId: String): List<MediaAssetEntity>

    @Query("SELECT * FROM media_assets WHERE id = :id")
    suspend fun getAssetById(id: String): MediaAssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: MediaAssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<MediaAssetEntity>)

    @Query("DELETE FROM media_assets WHERE id = :id")
    suspend fun deleteAssetById(id: String)

    @Query("SELECT COUNT(*) FROM media_assets WHERE sourceUri = :uri")
    suspend fun countAssetsWithUri(uri: String): Int

    @Query("SELECT COUNT(*) FROM media_assets WHERE projectId = :projectId")
    fun countAssetsForProject(projectId: String): Flow<Int>

    @Query("SELECT * FROM media_assets WHERE projectId = :projectId ORDER BY addedAt ASC LIMIT 1")
    fun getFirstAssetForProject(projectId: String): Flow<MediaAssetEntity?>

    @Query("SELECT DISTINCT sourceUri FROM media_assets WHERE projectId = :projectId")
    suspend fun getDistinctUrisForProject(projectId: String): List<String>
}
