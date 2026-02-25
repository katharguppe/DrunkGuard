package com.traffic.drunkguard.beta

import com.traffic.drunkguard.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton manager that tracks the beta mode state.
 * Injected via Hilt and observes SettingsRepository for changes.
 */
@Singleton
class BetaModeManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    private val _isBetaMode = MutableStateFlow(false)
    val isBetaMode: StateFlow<Boolean> = _isBetaMode.asStateFlow()

    /**
     * Initialize the beta mode state from settings.
     * Call this during app startup.
     */
    suspend fun initialize() {
        _isBetaMode.value = settingsRepository.isBetaMode()
    }

    /**
     * Refresh the beta mode state from settings.
     * Call this when settings might have changed.
     */
    suspend fun refresh() {
        _isBetaMode.value = settingsRepository.isBetaMode()
    }

    /**
     * Set beta mode in settings and update the state flow.
     */
    suspend fun setBetaMode(enabled: Boolean): Result<Unit> {
        return settingsRepository.setSetting(
            SettingsRepository.KEY_BETA_MODE,
            enabled.toString()
        ).also { result ->
            if (result.isSuccess) {
                _isBetaMode.value = enabled
            }
        }
    }

    /**
     * Synchronous check for beta mode.
     * Use this only when you can't use the flow (e.g., in non-coroutine contexts).
     * Prefer observing [isBetaMode] flow in UI layer.
     */
    fun isBetaModeSync(): Boolean = _isBetaMode.value
}
