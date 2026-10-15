package com.nutritrack.app.domain.calc

import com.nutritrack.app.domain.model.ActivityLevel
import com.nutritrack.app.domain.model.Gender
import com.nutritrack.app.domain.model.GoalType
import com.nutritrack.app.domain.model.MacroPreset
import com.nutritrack.app.domain.model.WorkoutIntensity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DomainCalculatorsTest {

    // 1. BmrCalculator
    @Test
    fun bmrCalculator_mifflinStJeor_accuratelyComputes() {
        // Male: 10 * 70 + 6.25 * 175 - 5 * 25 + 5 = 700 + 1093.75 - 125 + 5 = 1673.75
        val maleBmr = BmrCalculator.calculateBmr(70.0, 175.0, 25, Gender.MALE)
        assertEquals(1673.75, maleBmr, 0.01)

        // Female: 10 * 60 + 6.25 * 165 - 5 * 30 - 161 = 600 + 1031.25 - 150 - 161 = 1320.25
        val femaleBmr = BmrCalculator.calculateBmr(60.0, 165.0, 30, Gender.FEMALE)
        assertEquals(1320.25, femaleBmr, 0.01)
    }

    // 2. TdeeCalculator
    @Test
    fun tdeeCalculator_multipliersAppliedCorrectly() {
        val bmr = 1500.0
        assertEquals(1800.0, TdeeCalculator.calculateTdee(bmr, ActivityLevel.SEDENTARY), 0.01)
        assertEquals(2062.5, TdeeCalculator.calculateTdee(bmr, ActivityLevel.LIGHTLY_ACTIVE), 0.01)
        assertEquals(2325.0, TdeeCalculator.calculateTdee(bmr, ActivityLevel.MODERATELY_ACTIVE), 0.01)
        assertEquals(2587.5, TdeeCalculator.calculateTdee(bmr, ActivityLevel.VERY_ACTIVE), 0.01)
    }

    // 3. CalorieGoalCalculator
    @Test
    fun calorieGoalCalculator_paceAndSafetyLimits() {
        val tdee = 2400.0

        // Moderate weight loss: 0.5 kg/week -> 550 kcal deficit -> 1850 -> rounded to 1850
        val normalLoss = CalorieGoalCalculator.calculateTarget(tdee, GoalType.LOSE_WEIGHT, 0.5, Gender.MALE)
        assertEquals(1850, normalLoss.targetCalories)
        assertFalse(normalLoss.isDeficitCapped)
        assertFalse(normalLoss.isFloorApplied)

        // Aggressive weight loss deficit cap: 25% max deficit on 2000 TDEE = 500 kcal max deficit
        val aggressiveLoss = CalorieGoalCalculator.calculateTarget(2000.0, GoalType.LOSE_WEIGHT, 1.0, Gender.MALE)
        assertEquals(1500, aggressiveLoss.targetCalories) // 2000 - 500 = 1500
        assertTrue(aggressiveLoss.isDeficitCapped)

        // Female Floor: 1200 kcal floor enforced
        val lowTdeeFemale = CalorieGoalCalculator.calculateTarget(1300.0, GoalType.LOSE_WEIGHT, 0.5, Gender.FEMALE)
        assertEquals(1200, lowTdeeFemale.targetCalories)
        assertTrue(lowTdeeFemale.isFloorApplied)

        // Male Floor: 1500 kcal floor enforced
        val lowTdeeMale = CalorieGoalCalculator.calculateTarget(1600.0, GoalType.LOSE_WEIGHT, 0.5, Gender.MALE)
        assertEquals(1500, lowTdeeMale.targetCalories)
        assertTrue(lowTdeeMale.isFloorApplied)

        // Gain weight: 0.5 kg/week -> +550 kcal -> 2950
        val weightGain = CalorieGoalCalculator.calculateTarget(2400.0, GoalType.GAIN_WEIGHT, 0.5, Gender.MALE)
        assertEquals(2950, weightGain.targetCalories)

        // 10 kcal rounding test: 2133 -> 2130
        val roundingTest = CalorieGoalCalculator.calculateTarget(2133.0, GoalType.MAINTAIN_WEIGHT, 0.0, Gender.MALE)
        assertEquals(2130, roundingTest.targetCalories)
    }

    // 4. MacroCalculator
    @Test
    fun macroCalculator_presetsAndGrams() {
        val cals = 2000

        // Balanced: 50% Carbs, 25% Protein, 25% Fat
        val balanced = MacroCalculator.calculateMacros(cals, MacroPreset.BALANCED)
        assertEquals(250.0, balanced.carbsGrams, 0.1) // 1000 kcal / 4
        assertEquals(125.0, balanced.proteinGrams, 0.1) // 500 kcal / 4
        assertEquals(55.6, balanced.fatGrams, 0.1) // 500 kcal / 9 = 55.56
        assertEquals(56, balanced.roundedSplit.fatGrams)

        // High Protein: 40% Carbs, 30% Protein, 30% Fat
        val highProtein = MacroCalculator.calculateMacros(cals, MacroPreset.HIGH_PROTEIN)
        assertEquals(200.0, highProtein.carbsGrams, 0.1) // 800 kcal / 4
        assertEquals(150.0, highProtein.proteinGrams, 0.1) // 600 kcal / 4
        assertEquals(66.7, highProtein.fatGrams, 0.1) // 600 kcal / 9

        // Low Carb: 30% Carbs, 35% Protein, 35% Fat
        val lowCarb = MacroCalculator.calculateMacros(cals, MacroPreset.LOW_CARB)
        assertEquals(150.0, lowCarb.carbsGrams, 0.1) // 600 kcal / 4
        assertEquals(175.0, lowCarb.proteinGrams, 0.1) // 700 kcal / 4
        assertEquals(77.8, lowCarb.fatGrams, 0.1) // 700 kcal / 9
    }

    // 5. WaterGoalCalculator
    @Test
    fun waterGoalCalculator_roundingAndFloor() {
        // 70 kg * 35 ml = 2450 ml -> rounds to 2500 ml on 250ml grid
        assertEquals(2500, WaterGoalCalculator.calculateDailyWaterGoalMl(70.0))

        // 80 kg * 35 ml = 2800 ml -> rounds to 2750 or 3000? 2800 / 250 = 11.2 -> 11 * 250 = 2750
        assertEquals(2750, WaterGoalCalculator.calculateDailyWaterGoalMl(80.0))

        // Low weight: 35 kg * 35 ml = 1225 ml -> minimum floor 1500 ml enforced
        assertEquals(1500, WaterGoalCalculator.calculateDailyWaterGoalMl(35.0))
    }

    // 6. StepCalculator
    @Test
    fun stepCalculator_caloriesAndDistance() {
        val steps = 10000
        val weightKg = 70.0
        val heightCm = 175.0

        // Calories: 10000 * 70 * 0.00057 = 399 kcal
        val cals = StepCalculator.calculateCaloriesBurned(steps, weightKg)
        assertEquals(399.0, cals, 0.01)

        // Distance: 10000 * (175 * 0.415) / 100000 = 7.2625 km
        val dist = StepCalculator.calculateDistanceKm(steps, heightCm)
        assertEquals(7.26, dist, 0.01)
    }

    // 7. WorkoutCalculator
    @Test
    fun workoutCalculator_metFormulaAndStepDeduplication() {
        // Running 6 MET, 70 kg, 30 min, moderate intensity (1.0)
        // (6 * 3.5 * 70 / 200) * 30 = 220.5 kcal
        val exerciseKcal = WorkoutCalculator.calculateExerciseCalories(6.0, 70.0, 30, WorkoutIntensity.MODERATE)
        assertEquals(220.5, exerciseKcal, 0.1)

        // Vigorous intensity (1.2) = 220.5 * 1.2 = 264.6 kcal
        val vigorousKcal = WorkoutCalculator.calculateExerciseCalories(6.0, 70.0, 30, WorkoutIntensity.VIGOROUS)
        assertEquals(264.6, vigorousKcal, 0.1)

        // Step deduplication:
        // Raw step calories for 10,000 steps (70 kg) = 399.0 kcal
        // 30 min run accounts for 30 * 100 = 3,000 steps = 119.7 kcal
        // Deduplicated step calories = 399.0 - 119.7 = 279.3 kcal
        // Total burned = 220.5 (workout) + 279.3 (steps) = 499.8 kcal
        val totalBurned = WorkoutCalculator.calculateTotalBurnedCalories(
            totalWorkoutCalories = 220.5,
            rawStepCalories = 399.0,
            walkOrRunWorkoutMinutes = 30,
            weightKg = 70.0
        )
        assertEquals(499.8, totalBurned, 0.1)
    }

    // 8. BmiCalculator
    @Test
    fun bmiCalculator_classifiesAccurately() {
        val underweight = BmiCalculator.getBmiResult(45.0, 170.0)
        assertEquals(BmiCategory.UNDERWEIGHT, underweight.category)

        val healthy = BmiCalculator.getBmiResult(68.0, 175.0)
        assertEquals(BmiCategory.HEALTHY, healthy.category)

        val overweight = BmiCalculator.getBmiResult(85.0, 175.0)
        assertEquals(BmiCategory.OVERWEIGHT, overweight.category)

        val obese = BmiCalculator.getBmiResult(105.0, 175.0)
        assertEquals(BmiCategory.OBESE, obese.category)
    }

    // 9. StreakCalculator
    @Test
    fun streakCalculator_handlesRolloverAndBreaks() {
        val today = LocalDate.of(2026, 10, 15)

        // Scenario A: Logged today and yesterday -> streak = 2
        val datesA = setOf("2026-10-15", "2026-10-14")
        val streakA = StreakCalculator.calculateStreak(datesA, today)
        assertEquals(2, streakA.currentStreak)
        assertTrue(streakA.isLoggedToday)

        // Scenario B: Today not logged yet, but yesterday was -> streak preserved until midnight
        val datesB = setOf("2026-10-14", "2026-10-13", "2026-10-12")
        val streakB = StreakCalculator.calculateStreak(datesB, today)
        assertEquals(3, streakB.currentStreak)
        assertFalse(streakB.isLoggedToday)

        // Scenario C: Missed yesterday -> streak broken (0)
        val datesC = setOf("2026-10-13", "2026-10-12")
        val streakC = StreakCalculator.calculateStreak(datesC, today)
        assertEquals(0, streakC.currentStreak)
        assertEquals(2, streakC.bestStreak)
    }

    // 10. WeightTrendCalculator
    @Test
    fun weightTrendCalculator_movingAverageAndProjectedDate() {
        val entries = listOf(
            WeightDataPoint("2026-10-01", 80.0),
            WeightDataPoint("2026-10-02", 79.8),
            WeightDataPoint("2026-10-03", 79.5),
            WeightDataPoint("2026-10-04", 79.4),
            WeightDataPoint("2026-10-05", 79.1),
            WeightDataPoint("2026-10-06", 79.0),
            WeightDataPoint("2026-10-07", 78.8)
        )
        val ma = WeightTrendCalculator.calculateMovingAverage(entries, windowDays = 7)
        assertEquals(7, ma.size)
        // Average of all 7 days: 79.37 kg
        assertEquals(79.37, ma.last().second, 0.05)

        // Projection: 78.8 kg -> goal 74.8 kg (4 kg remaining at 0.5 kg/week = 8 weeks = 56 days)
        val today = LocalDate.of(2026, 10, 7)
        val trend = WeightTrendCalculator.calculateTrendAndProjection(
            currentWeightKg = 78.8,
            startWeightKg = 80.0,
            goalWeightKg = 74.8,
            goalType = GoalType.LOSE_WEIGHT,
            paceKgPerWeek = 0.5,
            today = today
        )
        assertTrue(trend.isMovingTowardGoal)
        assertNotNull(trend.projectedGoalDate)
        assertEquals(today.plusDays(56), trend.projectedGoalDate)

        // If weight moved away from goal (e.g. gained weight when goal is loss), projected date is null
        val reverseTrend = WeightTrendCalculator.calculateTrendAndProjection(
            currentWeightKg = 81.0,
            startWeightKg = 80.0,
            goalWeightKg = 75.0,
            goalType = GoalType.LOSE_WEIGHT,
            paceKgPerWeek = 0.5,
            today = today
        )
        assertFalse(reverseTrend.isMovingTowardGoal)
        assertNull(reverseTrend.projectedGoalDate)
    }

    // 11. AdherenceCalculator
    @Test
    fun adherenceCalculator_withinGoalWindow() {
        val goal = 2000

        // 90% is 1800, 110% is 2200
        assertTrue(AdherenceCalculator.isWithinGoal(1800, goal))
        assertTrue(AdherenceCalculator.isWithinGoal(2000, goal))
        assertTrue(AdherenceCalculator.isWithinGoal(2200, goal))

        assertFalse(AdherenceCalculator.isWithinGoal(1799, goal))
        assertFalse(AdherenceCalculator.isWithinGoal(2201, goal))

        assertEquals(80.0, AdherenceCalculator.calculateAdherenceRate(4, 5), 0.01)
    }

    // 12. ServingCalculator
    @Test
    fun servingCalculator_computesNutrientsSnapshot() {
        val computed = ServingCalculator.computeNutrients(
            baseCalories = 490.0,
            baseProtein = 22.0,
            baseCarbs = 60.0,
            baseFat = 17.0,
            baseFiber = 2.5,
            baseSugar = 1.0,
            baseSodiumMg = 650.0,
            servingMultiplier = 0.5, // Half plate
            quantity = 2.0 // 2 half plates = 1 full plate
        )
        assertEquals(490.0, computed.calories, 0.01)
        assertEquals(22.0, computed.protein, 0.01)
        assertEquals(60.0, computed.carbs, 0.01)
        assertEquals(17.0, computed.fat, 0.01)
        assertEquals(2.5, computed.fiber!!, 0.01)
        assertEquals(650.0, computed.sodiumMg!!, 0.01)
    }
}
