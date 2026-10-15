package com.nutritrack.app.domain.model

enum class FoodSource {
    SEED,
    USER,
    ONLINE
}

data class ServingOption(
    val id: String,
    val foodId: String,
    val label: String,
    val multiplier: Double
)

data class FoodItem(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    val category: String,
    val emoji: String,
    val servingLabel: String,
    val servingGrams: Double? = null,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodiumMg: Double? = null,
    val barcode: String? = null,
    val source: FoodSource = FoodSource.SEED,
    val isFavorite: Boolean = false,
    val lastUsedAt: Long? = null,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val servingOptions: List<ServingOption> = emptyList()
)
