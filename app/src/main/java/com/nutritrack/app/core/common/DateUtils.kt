package com.nutritrack.app.core.common

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateUtils {
    private val ISO_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val DISPLAY_HEADER_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH)
    private val SHORT_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

    fun toIsoDate(date: LocalDate = LocalDate.now()): String {
        return date.format(ISO_FORMATTER)
    }

    fun parseIsoDate(dateString: String): LocalDate {
        return try {
            LocalDate.parse(dateString, ISO_FORMATTER)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    fun formatDisplayDate(date: LocalDate): String {
        return date.format(DISPLAY_HEADER_FORMATTER)
    }

    fun getDayOfWeekLabel(date: LocalDate): String {
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "M"
            DayOfWeek.TUESDAY -> "T"
            DayOfWeek.WEDNESDAY -> "W"
            DayOfWeek.THURSDAY -> "T"
            DayOfWeek.FRIDAY -> "F"
            DayOfWeek.SATURDAY -> "S"
            DayOfWeek.SUNDAY -> "S"
        }
    }

    fun getShortDayName(date: LocalDate): String {
        return date.format(SHORT_DAY_FORMATTER)
    }

    /**
     * Returns the 7 days of the calendar week (Monday to Sunday) containing the given date.
     */
    fun getWeekDays(referenceDate: LocalDate): List<LocalDate> {
        val monday = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0..6).map { monday.plusDays(it.toLong()) }
    }

    /**
     * Returns the last 7 days ending at referenceDate.
     */
    fun getLast7Days(referenceDate: LocalDate = LocalDate.now()): List<LocalDate> {
        return (6 downTo 0).map { referenceDate.minusDays(it.toLong()) }
    }

    /**
     * Time-of-day greeting:
     * - Under 12:00: "Good morning"
     * - Under 18:00: "Good afternoon"
     * - 18:00 and after: "Good evening"
     */
    fun getGreeting(userName: String, time: LocalTime = LocalTime.now()): String {
        val greetingPrefix = when {
            time.hour < 12 -> "Good morning"
            time.hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
        val cleanName = userName.trim().split(" ").firstOrNull()?.replaceFirstChar { it.uppercase() }
        return if (!cleanName.isNullOrBlank()) {
            "$greetingPrefix, $cleanName"
        } else {
            greetingPrefix
        }
    }
}
