package com.traffic.drunkguard.di

import android.content.Context
import androidx.room.Room
import com.traffic.drunkguard.data.db.DrunkGuardDatabase
import com.traffic.drunkguard.data.db.OfficerDao
import com.traffic.drunkguard.data.db.SettingsDao
import com.traffic.drunkguard.data.db.VehicleCheckDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DrunkGuardDatabase {
        return Room.databaseBuilder(
            context,
            DrunkGuardDatabase::class.java,
            DrunkGuardDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideOfficerDao(database: DrunkGuardDatabase): OfficerDao =
        database.officerDao()

    @Provides
    fun provideVehicleCheckDao(database: DrunkGuardDatabase): VehicleCheckDao =
        database.vehicleCheckDao()

    @Provides
    fun provideSettingsDao(database: DrunkGuardDatabase): SettingsDao =
        database.settingsDao()
}
