package com.nutritrack.app.domain.model

data class Achievement(
    val key: String,
    val title: String,
    val description: String,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val maxProgress: Int = 1
) {
    val isUnlocked: Boolean get() = unlockedAt != null
}
