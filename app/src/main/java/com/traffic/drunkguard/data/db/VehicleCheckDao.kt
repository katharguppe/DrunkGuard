package com.traffic.drunkguard.data.db

import androidx.room.*
import com.traffic.drunkguard.data.model.VehicleCheck
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleCheckDao {

    @Query("SELECT * FROM vehicle_checks ORDER BY timestamp DESC")
    fun getAll(): Flow<List<VehicleCheck>>

    @Query("SELECT * FROM vehicle_checks WHERE officerId = :officerId ORDER BY timestamp DESC")
    fun getByOfficer(officerId: String): Flow<List<VehicleCheck>>

    @Query("SELECT * FROM vehicle_checks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VehicleCheck?

    @Query("""
        SELECT * FROM vehicle_checks
        WHERE timestamp BETWEEN :startDate AND :endDate
        ORDER BY timestamp DESC
    """)
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<VehicleCheck>>

    @Query("SELECT COUNT(*) FROM vehicle_checks WHERE officerId = :officerId AND DATE(timestamp/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayCount(officerId: String): Int

    @Query("SELECT COUNT(*) FROM vehicle_checks WHERE officerId = :officerId AND intoxicationLevel != 'SOBER' AND DATE(timestamp/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayViolations(officerId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(check: VehicleCheck)

    @Update
    suspend fun update(check: VehicleCheck)

    @Delete
    suspend fun delete(check: VehicleCheck)

    @Query("DELETE FROM vehicle_checks")
    suspend fun deleteAll()
}
