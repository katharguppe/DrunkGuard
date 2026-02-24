package com.traffic.drunkguard.data.db

import androidx.room.*
import com.traffic.drunkguard.data.model.Officer
import kotlinx.coroutines.flow.Flow

@Dao
interface OfficerDao {

    @Query("SELECT * FROM officers WHERE badgeId = :badgeId LIMIT 1")
    suspend fun getByBadgeId(badgeId: String): Officer?

    @Query("SELECT * FROM officers WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Officer?

    @Query("SELECT * FROM officers LIMIT 1")
    fun getCurrentOfficer(): Flow<Officer?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(officer: Officer)

    @Delete
    suspend fun delete(officer: Officer)

    @Query("DELETE FROM officers")
    suspend fun deleteAll()
}
