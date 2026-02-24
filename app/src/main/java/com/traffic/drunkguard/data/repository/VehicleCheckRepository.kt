package com.traffic.drunkguard.data.repository

import com.traffic.drunkguard.data.db.VehicleCheckDao
import com.traffic.drunkguard.data.model.VehicleCheck
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleCheckRepository @Inject constructor(
    private val vehicleCheckDao: VehicleCheckDao
) {
    fun getAllChecks(): Flow<List<VehicleCheck>> = vehicleCheckDao.getAll()

    fun getChecksByOfficer(officerId: String): Flow<List<VehicleCheck>> =
        vehicleCheckDao.getByOfficer(officerId)

    fun getChecksByDateRange(startDate: Long, endDate: Long): Flow<List<VehicleCheck>> =
        vehicleCheckDao.getByDateRange(startDate, endDate)

    suspend fun getCheckById(id: String): VehicleCheck? =
        vehicleCheckDao.getById(id)

    suspend fun getTodayStats(officerId: String): Pair<Int, Int> {
        val total = vehicleCheckDao.getTodayCount(officerId)
        val violations = vehicleCheckDao.getTodayViolations(officerId)
        return Pair(total, violations)
    }

    suspend fun saveCheck(check: VehicleCheck): Result<Unit> = try {
        vehicleCheckDao.insert(check)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateCheck(check: VehicleCheck): Result<Unit> = try {
        vehicleCheckDao.update(check)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteCheck(check: VehicleCheck): Result<Unit> = try {
        vehicleCheckDao.delete(check)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
