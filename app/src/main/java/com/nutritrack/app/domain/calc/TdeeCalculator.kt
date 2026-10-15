package com.nutritrack.app.domain.calc

import com.nutritrack.app.domain.model.ActivityLevel

object TdeeCalculator {
    /**
     * Calculates Total Daily Energy Expenditure (TDEE) from BMR and activity multiplier.
     */
    fun calculateTdee(bmr: Double, activityLevel: ActivityLevel): Double {
        return bmr * activityLevel.multiplier
    }

    fun calculateTdee(bmr: Double, multiplier: Double): Double {
        return bmr * multiplier
    }
}
