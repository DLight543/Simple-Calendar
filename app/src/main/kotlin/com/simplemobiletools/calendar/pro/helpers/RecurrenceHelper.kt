package com.simplemobiletools.calendar.pro.helpers

import com.simplemobiletools.calendar.pro.models.Event
import org.joda.time.DateTime
import com.simplemobiletools.calendar.pro.extensions.seconds

import com.simplemobiletools.calendar.pro.helpers.Frequency


class RecurrenceHelper {

    fun getOccurrences(event: Event, maxCount: Int = 1000): List<DateTime> {
        val occurrences = mutableListOf<DateTime>()
        // Convert timestamp to DateTime
        var currentDate = DateTime(event.startTS * 1000L)
        var count = 0

        while (shouldContinue(event, currentDate, count, maxCount)) {
            when (event.repeatInterval.getFrequency()) {
                Frequency.DAILY -> addDailyOccurrences(event, currentDate, occurrences)
                Frequency.WEEKLY -> addWeeklyOccurrences(event, currentDate, occurrences)
                Frequency.MONTHLY -> addMonthlyOccurrences(event, currentDate, occurrences)
                Frequency.YEARLY -> addYearlyOccurrences(event, currentDate, occurrences)
                else -> {} // Handle custom
            }
            currentDate = incrementDate(event, currentDate)
            count++
        }
        return occurrences
    }

    private fun addDailyOccurrences(event: Event, currentDate: DateTime, occurrences: MutableList<DateTime>) {
        occurrences.add(currentDate)
    }

    private fun addMonthlyOccurrences(event: Event, currentDate: DateTime, occurrences: MutableList<DateTime>) {
        when (event.repeatRule) {
            REPEAT_SAME_DAY -> occurrences.add(currentDate)
            REPEAT_LAST_DAY -> occurrences.add(currentDate.withDayOfMonth(currentDate.dayOfMonth().maximumValue))
            else -> handlePositionalMonthly(event, currentDate, occurrences)
        }
    }

    private fun handlePositionalMonthly(event: Event, baseDate: DateTime, occurrences: MutableList<DateTime>) {
        val (weekNumber, dayOfWeek) = parsePositionalRule(event.repeatRule)
        val candidateDate = baseDate.withDayOfMonth(1)
            .withDayOfWeek(dayOfWeek)
            .plusWeeks(weekNumber - 1)

        if (candidateDate.monthOfYear == baseDate.monthOfYear) {
            occurrences.add(candidateDate)
        }
    }

    private fun addYearlyOccurrences(event: Event, currentDate: DateTime, occurrences: MutableList<DateTime>) {
        occurrences.add(currentDate)
    }

    private fun parsePositionalRule(rule: Int): Pair<Int, Int> {
        val weekNumber = (rule shr 8) and 0xFF
        val dayOfWeek = rule and 0xFF
        return Pair(weekNumber, dayOfWeek)
    }
    // Add this extension function if not already present
    private fun DateTime.seconds() = millis / 1000L

    private fun shouldContinue(event: Event, currentDate: DateTime, count: Int, maxCount: Int): Boolean {
        return when {
            event.repeatLimit > 0 -> currentDate.seconds() <= event.repeatLimit
            event.repeatLimit < 0 -> count < -event.repeatLimit.toInt()
            else -> count < maxCount
        }
    }

    // Fix syntax error in weekly check
    private fun addWeeklyOccurrences(event: Event, currentDate: DateTime, occurrences: MutableList<DateTime>) {
        if ((event.repeatRule and (1 shl (currentDate.dayOfWeek - 1))) != 0) {
            occurrences.add(currentDate)
        }
    }

    // Make frequency check exhaustive
    private fun incrementDate(event: Event, currentDate: DateTime): DateTime {
        return when (event.repeatInterval.getFrequency()) {
            Frequency.DAILY -> currentDate.plusDays(event.repeatInterval)
            Frequency.WEEKLY -> currentDate.plusWeeks(event.repeatInterval / 7)
            Frequency.MONTHLY -> currentDate.plusMonths(1)
            Frequency.YEARLY -> currentDate.plusYears(1)
            Frequency.CUSTOM -> currentDate // Handle custom case if needed
        }
    }
}
