package com.traffic.drunkguard.data.repository

import com.traffic.drunkguard.data.db.OfficerDao
import com.traffic.drunkguard.data.model.Officer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfficerRepository @Inject constructor(
    private val officerDao: OfficerDao
) {
    fun getCurrentOfficer(): Flow<Officer?> = officerDao.getCurrentOfficer()

    suspend fun getByBadgeId(badgeId: String): Officer? =
        officerDao.getByBadgeId(badgeId)

    suspend fun saveOfficer(officer: Officer): Result<Unit> = try {
        officerDao.insert(officer)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteOfficer(officer: Officer): Result<Unit> = try {
        officerDao.delete(officer)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun clearAll(): Result<Unit> = try {
        officerDao.deleteAll()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
