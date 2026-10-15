package com.nutritrack.app.domain.model

enum class StepSource {
    AUTO,
    HEALTH_CONNECT,
    SENSOR,
    MANUAL
}

data class StepDaySummary(
    val date: String, // yyyy-MM-dd
    val sensorSteps: Int = 0,
    val healthConnectSteps: Int = 0,
    val manualSteps: Int? = null,
    val source: StepSource = StepSource.AUTO,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val effectiveSteps: Int
        get() = manualSteps ?: when (source) {
            StepSource.HEALTH_CONNECT -> healthConnectSteps
            StepSource.SENSOR -> sensorSteps
            StepSource.MANUAL -> manualSteps ?: 0
            StepSource.AUTO -> if (healthConnectSteps > 0) healthConnectSteps else sensorSteps
        }
}

data class StepSnapshot(
    val id: Long = 0,
    val timestamp: Long,
    val counterValue: Long
)
