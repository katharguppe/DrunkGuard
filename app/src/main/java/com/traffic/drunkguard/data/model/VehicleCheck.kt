package com.traffic.drunkguard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_checks")
data class VehicleCheck(
    @PrimaryKey
    val id: String,
    val officerId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val plateNumber: String,
    val vehicleType: String,
    val vehicleColor: String,
    val vehicleMake: String,
    val subjectPhotoPath: String,
    val intoxicationLevel: IntoxicationLevel,
    val confidenceScore: Float,
    val challanId: String? = null,
    val pdfPath: String? = null,
    val whatsappSent: Boolean = false,
    val isMockData: Boolean = false
)
