package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        MediaAssetEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class StudioDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun mediaAssetDao(): MediaAssetDao

    companion object {
        @Volatile
        private var instance: StudioDatabase? = null

        fun getInstance(context: Context): StudioDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudioDatabase::class.java,
                    "mad_creatives_studio.db"
                ).build().also { instance = it }
            }
        }
    }
}
