package com.traffic.drunkguard.data.repository

import com.traffic.drunkguard.data.db.SettingsDao
import com.traffic.drunkguard.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {
    companion object {
        const val KEY_FINE_SLIGHTLY = "fine_slightly"
        const val KEY_FINE_MODERATELY = "fine_moderately"
        const val KEY_FINE_HEAVILY = "fine_heavily"
        const val KEY_CONFIDENCE_THRESHOLD = "confidence_threshold"
        const val KEY_STATION_NAME = "station_name"
        const val KEY_BETA_MODE = "beta_mode"
        const val KEY_DARK_MODE = "dark_mode"

        const val DEFAULT_FINE_SLIGHTLY = "1000"
        const val DEFAULT_FINE_MODERATELY = "3000"
        const val DEFAULT_FINE_HEAVILY = "5000"
        const val DEFAULT_CONFIDENCE_THRESHOLD = "0.65"
        const val DEFAULT_STATION_NAME = "Traffic Police Station"
        const val DEFAULT_BETA_MODE = "false"
        const val DEFAULT_DARK_MODE = "false"
    }

    fun getSetting(key: String): Flow<String?> =
        settingsDao.getFlow(key).map { it?.value }

    suspend fun getSettingSync(key: String, defaultValue: String): String =
        settingsDao.get(key)?.value ?: defaultValue

    suspend fun setSetting(key: String, value: String): Result<Unit> = try {
        settingsDao.set(AppSettings(key, value))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getFineAmounts(): Triple<Int, Int, Int> {
        val slightly = getSettingSync(KEY_FINE_SLIGHTLY, DEFAULT_FINE_SLIGHTLY).toIntOrNull() ?: 1000
        val moderately = getSettingSync(KEY_FINE_MODERATELY, DEFAULT_FINE_MODERATELY).toIntOrNull() ?: 3000
        val heavily = getSettingSync(KEY_FINE_HEAVILY, DEFAULT_FINE_HEAVILY).toIntOrNull() ?: 5000
        return Triple(slightly, moderately, heavily)
    }

    suspend fun getConfidenceThreshold(): Float =
        getSettingSync(KEY_CONFIDENCE_THRESHOLD, DEFAULT_CONFIDENCE_THRESHOLD).toFloatOrNull() ?: 0.65f

    suspend fun isBetaMode(): Boolean =
        getSettingSync(KEY_BETA_MODE, DEFAULT_BETA_MODE).toBoolean()

    suspend fun isDarkMode(): Boolean =
        getSettingSync(KEY_DARK_MODE, DEFAULT_DARK_MODE).toBoolean()

    suspend fun initializeDefaults() {
        if (settingsDao.get(KEY_FINE_SLIGHTLY) == null) {
            setSetting(KEY_FINE_SLIGHTLY, DEFAULT_FINE_SLIGHTLY)
        }
        if (settingsDao.get(KEY_FINE_MODERATELY) == null) {
            setSetting(KEY_FINE_MODERATELY, DEFAULT_FINE_MODERATELY)
        }
        if (settingsDao.get(KEY_FINE_HEAVILY) == null) {
            setSetting(KEY_FINE_HEAVILY, DEFAULT_FINE_HEAVILY)
        }
        if (settingsDao.get(KEY_CONFIDENCE_THRESHOLD) == null) {
            setSetting(KEY_CONFIDENCE_THRESHOLD, DEFAULT_CONFIDENCE_THRESHOLD)
        }
        if (settingsDao.get(KEY_STATION_NAME) == null) {
            setSetting(KEY_STATION_NAME, DEFAULT_STATION_NAME)
        }
        if (settingsDao.get(KEY_BETA_MODE) == null) {
            setSetting(KEY_BETA_MODE, DEFAULT_BETA_MODE)
        }
        if (settingsDao.get(KEY_DARK_MODE) == null) {
            setSetting(KEY_DARK_MODE, DEFAULT_DARK_MODE)
        }
    }
}
