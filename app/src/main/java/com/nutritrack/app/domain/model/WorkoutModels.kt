package com.nutritrack.app.domain.model

enum class WorkoutIntensity(val multiplier: Double, val displayName: String) {
    LIGHT(0.8, "Light"),
    MODERATE(1.0, "Moderate"),
    VIGOROUS(1.2, "Vigorous")
}

data class WorkoutEntry(
    val id: String,
    val date: String, // yyyy-MM-dd
    val startTime: Long? = null,
    val activityKey: String,
    val name: String,
    val minutes: Int,
    val intensity: WorkoutIntensity = WorkoutIntensity.MODERATE,
    val met: Double,
    val calories: Double,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
