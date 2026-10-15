package com.nutritrack.app.domain.model

enum class MealType(val displayName: String, val emoji: String) {
    BREAKFAST("Breakfast", "🍳"),
    LUNCH("Lunch", "🥗"),
    DINNER("Dinner", "🍽️"),
    SNACKS("Snacks", "🍿")
}

data class DiaryEntry(
    val id: String,
    val date: String, // ISO date: yyyy-MM-dd
    val meal: MealType,
    val foodId: String? = null,
    val nameSnapshot: String,
    val emojiSnapshot: String,
    val servingLabelSnapshot: String,
    val quantity: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodiumMg: Double? = null,
    val isQuickAdd: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
