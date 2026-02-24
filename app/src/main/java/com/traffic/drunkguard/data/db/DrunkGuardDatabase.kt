package com.traffic.drunkguard.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.traffic.drunkguard.data.model.AppSettings
import com.traffic.drunkguard.data.model.IntoxicationLevel
import com.traffic.drunkguard.data.model.Officer
import com.traffic.drunkguard.data.model.VehicleCheck

@Database(
    entities = [Officer::class, VehicleCheck::class, AppSettings::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DrunkGuardDatabase : RoomDatabase() {

    abstract fun officerDao(): OfficerDao
    abstract fun vehicleCheckDao(): VehicleCheckDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "drunkguard.db"
    }
}

class Converters {
    @TypeConverter
    fun fromIntoxicationLevel(level: IntoxicationLevel): String = level.name

    @TypeConverter
    fun toIntoxicationLevel(value: String): IntoxicationLevel =
        IntoxicationLevel.valueOf(value)
}
