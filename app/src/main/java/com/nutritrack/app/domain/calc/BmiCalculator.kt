package com.nutritrack.app.domain.calc

enum class BmiCategory(val label: String) {
    UNDERWEIGHT("Underweight"),
    HEALTHY("Healthy"),
    OVERWEIGHT("Overweight"),
    OBESE("Obese")
}

data class BmiResult(
    val bmi: Double,
    val category: BmiCategory
)

object BmiCalculator {
    /**
     * Calculates BMI: kg / (m * m)
     */
    fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        val heightM = heightCm / 100.0
        if (heightM <= 0.0 || weightKg <= 0.0) return 0.0
        return weightKg / (heightM * heightM)
    }

    /**
     * Categorizes BMI per WHO standard guidelines:
     * - Underweight: < 18.5
     * - Healthy: 18.5 - 24.9
     * - Overweight: 25.0 - 29.9
     * - Obese: >= 30.0
     */
    fun classifyBmi(bmi: Double): BmiCategory {
        return when {
            bmi < 18.5 -> BmiCategory.UNDERWEIGHT
            bmi < 25.0 -> BmiCategory.HEALTHY
            bmi < 30.0 -> BmiCategory.OVERWEIGHT
            else -> BmiCategory.OBESE
        }
    }

    fun getBmiResult(weightKg: Double, heightCm: Double): BmiResult {
        val bmi = calculateBmi(weightKg, heightCm)
        return BmiResult(bmi = bmi, category = classifyBmi(bmi))
    }
}
