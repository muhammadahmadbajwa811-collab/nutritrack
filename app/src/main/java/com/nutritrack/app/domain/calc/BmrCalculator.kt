package com.nutritrack.app.domain.calc

import com.nutritrack.app.domain.model.Gender

object BmrCalculator {
    /**
     * Calculates Basal Metabolic Rate (BMR) using the Mifflin-St Jeor equation.
     * Male:   10 * weight(kg) + 6.25 * height(cm) - 5 * age + 5
     * Female: 10 * weight(kg) + 6.25 * height(cm) - 5 * age - 161
     */
    fun calculateBmr(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        gender: Gender
    ): Double {
        val safeAge = maxOf(0, age)
        val safeWeight = maxOf(0.0, weightKg)
        val safeHeight = maxOf(0.0, heightCm)
        val base = (10.0 * safeWeight) + (6.25 * safeHeight) - (5.0 * safeAge)
        return when (gender) {
            Gender.MALE -> base + 5.0
            Gender.FEMALE -> base - 161.0
        }
    }
}
