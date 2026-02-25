package com.traffic.drunkguard.di

import android.content.Context
import com.traffic.drunkguard.ml.TFLiteInferenceHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for ML (TensorFlow Lite) components
 */
@Module
@InstallIn(SingletonComponent::class)
object MLModule {

    /**
     * Provides TFLiteInferenceHelper as a singleton.
     * The helper loads the model on initialization.
     *
     * Note: If model loading fails, the helper will have isModelLoaded = false,
     * but will not crash. Call loadModel() explicitly to handle errors.
     */
    @Provides
    @Singleton
    fun provideTFLiteInferenceHelper(
        @ApplicationContext context: Context
    ): TFLiteInferenceHelper {
        return TFLiteInferenceHelper(context)
    }
}
