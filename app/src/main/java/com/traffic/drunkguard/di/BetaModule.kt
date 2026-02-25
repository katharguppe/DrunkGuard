package com.traffic.drunkguard.di

import android.content.Context
import com.traffic.drunkguard.beta.BetaModeManager
import com.traffic.drunkguard.utils.BetaMockProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for beta-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object BetaModule {

    @Provides
    @Singleton
    fun provideBetaMockProvider(
        @ApplicationContext context: Context
    ): BetaMockProvider {
        return BetaMockProvider.getInstance(context)
    }
}
