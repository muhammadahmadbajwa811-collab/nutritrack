package com.nutritrack.app.domain.calc

import com.nutritrack.app.domain.model.MacroPreset
import com.nutritrack.app.domain.model.MacroSplit
import kotlin.math.roundToInt

data class DetailedMacroSplit(
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val proteinCalories: Double,
    val carbsCalories: Double,
    val fatCalories: Double,
    val proteinPercentage: Int,
    val carbsPercentage: Int,
    val fatPercentage: Int
) {
    val roundedSplit: MacroSplit
        get() = MacroSplit(
            proteinGrams = proteinGrams.roundToInt(),
            carbsGrams = carbsGrams.roundToInt(),
            fatGrams = fatGrams.roundToInt()
        )
}

object MacroCalculator {
    const val KCAL_PER_GRAM_PROTEIN = 4.0
    const val KCAL_PER_GRAM_CARBS = 4.0
    const val KCAL_PER_GRAM_FAT = 9.0

    /**
     * Calculates macronutrient targets for a given calorie goal and macro preset.
     */
    fun calculateMacros(
        totalCalories: Int,
        preset: MacroPreset,
        customCarbsPct: Int = 50,
        customProteinPct: Int = 25,
        customFatPct: Int = 25
    ): DetailedMacroSplit {
        val (carbsPct, proteinPct, fatPct) = when (preset) {
            MacroPreset.BALANCED -> Triple(50, 25, 25)
            MacroPreset.HIGH_PROTEIN -> Triple(40, 30, 30)
            MacroPreset.LOW_CARB -> Triple(30, 35, 35)
            MacroPreset.CUSTOM -> Triple(customCarbsPct, customProteinPct, customFatPct)
        }

        val carbsCalories = totalCalories * (carbsPct / 100.0)
        val proteinCalories = totalCalories * (proteinPct / 100.0)
        val fatCalories = totalCalories * (fatPct / 100.0)

        val carbsGrams = carbsCalories / KCAL_PER_GRAM_CARBS
        val proteinGrams = proteinCalories / KCAL_PER_GRAM_PROTEIN
        val fatGrams = fatCalories / KCAL_PER_GRAM_FAT

        return DetailedMacroSplit(
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            proteinCalories = proteinCalories,
            carbsCalories = carbsCalories,
            fatCalories = fatCalories,
            proteinPercentage = proteinPct,
            carbsPercentage = carbsPct,
            fatPercentage = fatPct
        )
    }

    /**
     * Standard MacroSplit helper returning rounded integer grams.
     */
    fun calculateMacroSplit(totalCalories: Int, preset: MacroPreset = MacroPreset.BALANCED): MacroSplit {
        return calculateMacros(totalCalories, preset).roundedSplit
    }
}
