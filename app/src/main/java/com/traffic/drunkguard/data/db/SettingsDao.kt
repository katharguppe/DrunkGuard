package com.traffic.drunkguard.data.db

import androidx.room.*
import com.traffic.drunkguard.data.model.AppSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Query("SELECT * FROM app_settings WHERE key = :key LIMIT 1")
    suspend fun get(key: String): AppSettings?

    @Query("SELECT * FROM app_settings WHERE key = :key LIMIT 1")
    fun getFlow(key: String): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(setting: AppSettings)

    @Query("DELETE FROM app_settings WHERE key = :key")
    suspend fun delete(key: String)

    @Query("SELECT * FROM app_settings")
    fun getAll(): Flow<List<AppSettings>>
}
