package com.nutritrack.app.domain.calc

import java.time.LocalDate

data class StreakResult(
    val currentStreak: Int,
    val bestStreak: Int,
    val isLoggedToday: Boolean
)

object StreakCalculator {
    /**
     * Calculates consecutive day logging streak based on distinct dates of diary entries.
     * Rule: Consecutive local days with at least one diary entry;
     * today does not break the streak until midnight passes.
     *
     * @param loggedDates Set of local ISO dates ("yyyy-MM-dd") with at least one logged food
     * @param today The local date for "today"
     */
    fun calculateStreak(
        loggedDates: Set<String>,
        today: LocalDate = LocalDate.now()
    ): StreakResult {
        if (loggedDates.isEmpty()) {
            return StreakResult(currentStreak = 0, bestStreak = 0, isLoggedToday = false)
        }

        val parsedDates = loggedDates.mapNotNull {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                null
            }
        }.toSet()

        val isLoggedToday = parsedDates.contains(today)
        val yesterday = today.minusDays(1)
        val isLoggedYesterday = parsedDates.contains(yesterday)

        // Calculate current streak
        var currentStreak = 0
        if (isLoggedToday) {
            currentStreak = 1
            var checkDate = yesterday
            while (parsedDates.contains(checkDate)) {
                currentStreak++
                checkDate = checkDate.minusDays(1)
            }
        } else if (isLoggedYesterday) {
            // Today not logged yet, but yesterday was: streak is preserved until midnight
            currentStreak = 1
            var checkDate = yesterday.minusDays(1)
            while (parsedDates.contains(checkDate)) {
                currentStreak++
                checkDate = checkDate.minusDays(1)
            }
        } else {
            currentStreak = 0
        }

        // Calculate best (longest historical) streak
        val sortedDates = parsedDates.sorted()
        var bestStreak = 0
        var tempStreak = 0
        var prevDate: LocalDate? = null

        for (date in sortedDates) {
            if (prevDate == null) {
                tempStreak = 1
            } else if (date == prevDate.plusDays(1)) {
                tempStreak++
            } else if (date != prevDate) {
                tempStreak = 1
            }
            if (tempStreak > bestStreak) {
                bestStreak = tempStreak
            }
            prevDate = date
        }

        bestStreak = maxOf(bestStreak, currentStreak)

        return StreakResult(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            isLoggedToday = isLoggedToday
        )
    }
}
