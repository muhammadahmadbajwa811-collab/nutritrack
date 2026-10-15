package com.nutritrack.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_days")
data class StepDayEntity(
    @PrimaryKey
    val date: String, // yyyy-MM-dd
    val sensorSteps: Int = 0,
    val healthConnectSteps: Int = 0,
    val manualSteps: Int? = null,
    val source: String = "AUTO", // "AUTO", "HEALTH_CONNECT", "SENSOR", "MANUAL"
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "step_snapshots")
data class StepSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val counterValue: Long
)
