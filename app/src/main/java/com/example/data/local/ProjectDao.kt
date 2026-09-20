package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query(
        """
        SELECT 
            p.id,
            p.name,
            p.createdAt,
            p.updatedAt,
            p.aspectRatio,
            COUNT(m.id) AS clipCount,
            (SELECT m2.id FROM media_assets m2 WHERE m2.projectId = p.id ORDER BY m2.addedAt ASC LIMIT 1) AS firstAssetId,
            (SELECT m2.sourceUri FROM media_assets m2 WHERE m2.projectId = p.id ORDER BY m2.addedAt ASC LIMIT 1) AS firstAssetSourceUri
        FROM projects p
        LEFT JOIN media_assets m ON p.id = m.projectId
        GROUP BY p.id
        ORDER BY p.updatedAt DESC
        """
    )
    fun getAllProjectsWithStats(): Flow<List<ProjectWithStats>>

    @Query(
        """
        SELECT 
            p.id,
            p.name,
            p.createdAt,
            p.updatedAt,
            p.aspectRatio,
            COUNT(m.id) AS clipCount,
            (SELECT m2.id FROM media_assets m2 WHERE m2.projectId = p.id ORDER BY m2.addedAt ASC LIMIT 1) AS firstAssetId,
            (SELECT m2.sourceUri FROM media_assets m2 WHERE m2.projectId = p.id ORDER BY m2.addedAt ASC LIMIT 1) AS firstAssetSourceUri
        FROM projects p
        LEFT JOIN media_assets m ON p.id = m.projectId
        WHERE p.id = :id
        GROUP BY p.id
        """
    )
    fun getProjectWithStatsById(id: String): Flow<ProjectWithStats?>

    @Query(
        """
        SELECT 
            p.id,
            p.name,
            p.createdAt,
            p.updatedAt,
            p.aspectRatio,
            COUNT(m.id) AS clipCount,
            (SELECT m2.id FROM media_assets m2 WHERE m2.projectId = p.id ORDER BY m2.addedAt ASC LIMIT 1) AS firstAssetId,
            (SELECT m2.sourceUri FROM media_assets m2 WHERE m2.projectId = p.id ORDER BY m2.addedAt ASC LIMIT 1) AS firstAssetSourceUri
        FROM projects p
        LEFT JOIN media_assets m ON p.id = m.projectId
        WHERE p.id = :id
        GROUP BY p.id
        """
    )
    suspend fun getProjectWithStatsByIdOnce(id: String): ProjectWithStats?

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectEntityById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("UPDATE projects SET name = :name, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProjectName(id: String, name: String, updatedAt: Long)

    @Query("UPDATE projects SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProjectTimestamp(id: String, updatedAt: Long)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}
