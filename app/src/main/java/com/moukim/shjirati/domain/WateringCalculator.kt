package com.moukim.shjirati.domain

import com.moukim.shjirati.data.local.PlantEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object WateringCalculator {
    fun isDueToday(plant: PlantEntity, today: LocalDate = LocalDate.now()): Boolean {
        val lastWatered = plant.lastWateredAtEpochMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        // Never show a plant as due again on the same day after it was watered.
        if (lastWatered == today) return false

        if (plant.wateringDaysMask != 0) {
            val bit = 1 shl (today.dayOfWeek.value - 1)
            return plant.wateringDaysMask and bit != 0
        }

        val interval = plant.wateringIntervalDays ?: return false
        if (lastWatered == null) return true

        return !today.isBefore(lastWatered.plusDays(interval.toLong()))
    }

    fun weekdayMask(days: Set<DayOfWeek>): Int =
        days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }
}
