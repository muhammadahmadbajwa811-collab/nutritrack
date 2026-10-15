package com.nutritrack.app.domain.calc

import com.nutritrack.app.domain.model.ActivityLevel
import com.nutritrack.app.domain.model.Gender
import com.nutritrack.app.domain.model.Goal
import com.nutritrack.app.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieCalculatorTest {

    @Test
    fun calculateBmr_forMale_calculatesAccurately() {
        // Mifflin-St Jeor: 10 * 70 + 6.25 * 175 - 5 * 25 + 5 = 1673.75
        val bmr = CalorieCalculator.calculateBmr(
            weightKg = 70.0,
            heightCm = 175.0,
            age = 25,
            gender = Gender.MALE
        )
        assertEquals(1673.75, bmr, 0.01)
    }

    @Test
    fun calculateBmr_forFemale_calculatesAccurately() {
        // Mifflin-St Jeor: 10 * 60 + 6.25 * 165 - 5 * 30 - 161 = 1320.25
        val bmr = CalorieCalculator.calculateBmr(
            weightKg = 60.0,
            heightCm = 165.0,
            age = 30,
            gender = Gender.FEMALE
        )
        assertEquals(1320.25, bmr, 0.01)
    }

    @Test
    fun calculateTdee_appliesActivityMultiplierCorrectly() {
        val bmr = 1600.0
        val tdee = CalorieCalculator.calculateTdee(bmr, ActivityLevel.MODERATELY_ACTIVE.multiplier)
        assertEquals(2480.0, tdee, 0.01)
    }

    @Test
    fun calculateDailyCalorieTarget_weightLoss_enforcesMinimumFloor() {
        // Low weight & height female where deficit would drop below 1200
        val profile = UserProfile(
            age = 45,
            gender = Gender.FEMALE,
            heightCm = 145.0,
            weightKg = 42.0,
            activityLevel = ActivityLevel.SEDENTARY,
            goal = Goal.LOSE_WEIGHT
        )
        val target = CalorieCalculator.calculateDailyCalorieTarget(profile)
        assertTrue(
            "Calorie target should never drop below safe floor of 1200 kcal",
            target >= CalorieCalculator.MINIMUM_CALORIE_FLOOR
        )
    }

    @Test
    fun calculateMacroSplit_calculatesAccurateGrams() {
        val split = CalorieCalculator.calculateMacroSplit(2000)
        assertEquals(150, split.proteinGrams) // 600 kcal / 4
        assertEquals(200, split.carbsGrams)   // 800 kcal / 4
        assertEquals(67, split.fatGrams)      // 600 kcal / 9 = 66.67 -> 67
    }

    @Test
    fun calculateBmi_calculatesAccurately() {
        val bmi = CalorieCalculator.calculateBmi(weightKg = 70.0, heightCm = 175.0)
        assertEquals(22.86, bmi, 0.01)
    }

    @Test
    fun calculateDailyWaterGoalMl_recommends35mlPerKg() {
        // 70.0 kg * 35 ml = 2,450 ml -> rounded to nearest 250 ml = 2,500 ml
        val waterMl = CalorieCalculator.calculateDailyWaterGoalMl(70.0)
        assertEquals(2500, waterMl)
    }
}
