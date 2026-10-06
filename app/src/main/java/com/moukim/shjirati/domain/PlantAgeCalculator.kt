package com.moukim.shjirati.domain

import com.moukim.shjirati.data.local.PlantCategory
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object PlantAgeCalculator {

    fun calculateAge(
        plantedAtEpochMillis: Long?,
        category: PlantCategory,
        today: LocalDate = LocalDate.now()
    ): String? {
        if (plantedAtEpochMillis == null) return null

        val plantingDate = Instant.ofEpochMilli(plantedAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        if (plantingDate.isAfter(today)) {
            return "لم تُغرس بعد"
        }

        val totalDays = ChronoUnit.DAYS.between(plantingDate, today)
        if (totalDays == 0L) {
            return "حديث الغرس"
        }

        val period = Period.between(plantingDate, today)

        return when (category) {
            PlantCategory.TREE -> formatTreeAge(period, totalDays)
            PlantCategory.VEGETABLE -> formatVegetableAge(period, totalDays)
        }
    }

    private fun formatTreeAge(period: Period, totalDays: Long): String {
        val years = period.years
        val months = period.months

        if (years == 0 && months == 0) {
            return formatDays(totalDays.toInt())
        }

        val yearsPart = if (years > 0) formatYears(years) else null
        val monthsPart = if (months > 0) formatMonths(months) else null

        return when {
            yearsPart != null && monthsPart != null -> "$yearsPart و$monthsPart"
            yearsPart != null -> yearsPart
            monthsPart != null -> monthsPart
            else -> formatDays(totalDays.toInt())
        }
    }

    private fun formatVegetableAge(period: Period, totalDays: Long): String {
        val totalMonths = period.years * 12 + period.months
        val remainingDays = period.days

        if (totalDays < 30) {
            return formatDays(totalDays.toInt())
        }

        val monthsPart = if (totalMonths > 0) formatMonths(totalMonths) else null
        val daysPart = if (remainingDays > 0) formatDays(remainingDays) else null

        return when {
            monthsPart != null && daysPart != null -> "$monthsPart و$daysPart"
            monthsPart != null -> monthsPart
            daysPart != null -> daysPart
            else -> formatDays(totalDays.toInt())
        }
    }

    private fun formatYears(years: Int): String {
        return when (years) {
            1 -> "سنة واحدة"
            2 -> "سنتان"
            in 3..10 -> "$years سنوات"
            else -> "$years سنة"
        }
    }

    private fun formatMonths(months: Int): String {
        return when (months) {
            1 -> "شهر واحد"
            2 -> "شهران"
            in 3..10 -> "$months أشهر"
            else -> "$months شهرًا"
        }
    }

    private fun formatDays(days: Int): String {
        return when (days) {
            1 -> "يوم واحد"
            2 -> "يومان"
            in 3..10 -> "$days أيام"
            else -> "$days يومًا"
        }
    }
}
