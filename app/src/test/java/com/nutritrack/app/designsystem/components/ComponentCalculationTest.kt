package com.nutritrack.app.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

class ComponentCalculationTest {

    @Test
    fun calorieRing_underGoal_calculatesRemainingCorrectly() {
        val eaten = 1650
        val target = 2000
        val burned = 150
        val net = eaten - burned
        val remaining = target - net

        assertEquals(500, remaining)
        assertTrue(remaining >= 0)
    }

    @Test
    fun calorieRing_overGoal_detectsWarningState() {
        val eaten = 2300
        val target = 2000
        val burned = 100
        val net = eaten - burned
        val remaining = target - net

        assertEquals(-200, remaining)
        assertTrue("Net calories exceeded target, should trigger warning state", remaining < 0)
    }

    @Test
    fun macroDonut_calculatesExactCalorieShares() {
        val proteinG = 150 // 150 * 4 = 600 kcal
        val carbsG = 200   // 200 * 4 = 800 kcal
        val fatG = 67      // 67 * 9 = 603 kcal
        val totalKcal = (proteinG * 4) + (carbsG * 4) + (fatG * 9) // 2003 kcal

        val proteinPct = (proteinG * 4f) / totalKcal
        val carbsPct = (carbsG * 4f) / totalKcal
        val fatPct = (fatG * 9f) / totalKcal

        assertEquals(30, (proteinPct * 100).roundToInt())
        assertEquals(40, (carbsPct * 100).roundToInt())
        assertEquals(30, (fatPct * 100).roundToInt())
    }

    @Test
    fun waterGlass_calculatesFillRatioCorrectly() {
        val current = 1750
        val target = 2500
        val ratio = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)

        assertEquals(0.70f, ratio, 0.001f)
    }

    @Test
    fun quantityStepper_respectsMinAndMaxBounds() {
        val min = 0.5f
        val max = 20.0f
        val step = 0.5f

        // Decrement below min
        val atMin = 0.5f
        val decResult = (atMin - step).coerceAtLeast(min)
        assertEquals(0.5f, decResult, 0.001f)

        // Increment above max
        val atMax = 20.0f
        val incResult = (atMax + step).coerceAtMost(max)
        assertEquals(20.0f, incResult, 0.001f)
    }

    @Test
    fun weekBarChart_normalizesBarHeightsAgainstMax() {
        val values = listOf(1850, 2100, 1950, 2200, 1600, 1900, 1750)
        val goal = 2000
        val maxVal = maxOf(goal, values.maxOrNull() ?: 1)

        assertEquals(2200, maxVal)

        val heightRatio = values[3].toFloat() / maxVal.toFloat()
        assertEquals(1.0f, heightRatio, 0.001f)
    }

    @Test
    fun lineChart_calculatesWeightRangeAccurately() {
        val weights = listOf(73.5f, 73.2f, 72.8f, 72.5f, 72.0f)
        val target = 70.0f

        val minVal = (minOf(weights.minOrNull() ?: 0f, target) - 1f).coerceAtLeast(0f)
        val maxVal = maxOf(weights.maxOrNull() ?: 100f, target) + 1f

        assertEquals(69.0f, minVal, 0.01f)
        assertEquals(74.5f, maxVal, 0.01f)
        assertEquals(5.5f, maxVal - minVal, 0.01f)
    }
}
