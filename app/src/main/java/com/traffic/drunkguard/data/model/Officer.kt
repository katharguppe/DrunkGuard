package com.traffic.drunkguard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "officers")
data class Officer(
    @PrimaryKey
    val id: String,
    val badgeId: String,
    val name: String,
    val station: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)
