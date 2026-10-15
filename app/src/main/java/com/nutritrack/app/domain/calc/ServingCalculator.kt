package com.nutritrack.app.domain.calc

data class ComputedNutrition(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodiumMg: Double? = null
)

object ServingCalculator {
    /**
     * Calculates snapshot entry nutrients:
     * base nutrient * servingMultiplier * quantity
     */
    fun computeNutrients(
        baseCalories: Double,
        baseProtein: Double,
        baseCarbs: Double,
        baseFat: Double,
        baseFiber: Double? = null,
        baseSugar: Double? = null,
        baseSodiumMg: Double? = null,
        servingMultiplier: Double,
        quantity: Double
    ): ComputedNutrition {
        val factor = servingMultiplier * quantity
        return ComputedNutrition(
            calories = baseCalories * factor,
            protein = baseProtein * factor,
            carbs = baseCarbs * factor,
            fat = baseFat * factor,
            fiber = baseFiber?.let { it * factor },
            sugar = baseSugar?.let { it * factor },
            sodiumMg = baseSodiumMg?.let { it * factor }
        )
    }
}
