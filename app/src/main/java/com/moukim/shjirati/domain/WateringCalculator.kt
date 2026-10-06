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

        if (lastWatered == today) return false

        if (plant.wateringDaysMask != 0) {
            val bit = 1 shl (today.dayOfWeek.value - 1)
            return plant.wateringDaysMask and bit != 0
        }

        val interval = intervalFor(plant, today) ?: return false
        if (lastWatered == null) return true

        return !today.isBefore(lastWatered.plusDays(interval.toLong()))
    }

    fun intervalFor(plant: PlantEntity, date: LocalDate): Int? {
        if (!plant.seasonalScheduleEnabled) return plant.wateringIntervalDays

        return when (currentSeason(date)) {
            Season.SPRING -> plant.springIntervalDays ?: plant.wateringIntervalDays
            Season.SUMMER -> plant.summerIntervalDays ?: plant.wateringIntervalDays
            Season.AUTUMN -> plant.autumnIntervalDays ?: plant.wateringIntervalDays
            Season.WINTER -> plant.winterIntervalDays ?: plant.wateringIntervalDays
        }
    }

    /**
     * يحسب الجداول التلقائية الموصى بها مسبقاً لكل فصل بناءً على المعدل الأساسي للسقي:
     * - الصيف: سقي أكثر تكراراً (60% من الفترة الأساسية)
     * - الربيع: الفترة الأساسية (100%)
     * - الخريف: سقي أقل تكراراً (125% من الفترة الأساسية)
     * - الشتاء: سقي متباعد (180% من الفترة الأساسية)
     */
    fun defaultSeasonalIntervals(baseInterval: Int): SeasonalIntervals {
        val spring = baseInterval.coerceAtLeast(1)
        val summer = (baseInterval * 0.6).toInt().coerceAtLeast(1)
        val autumn = (baseInterval * 1.25).toInt().coerceAtLeast(1)
        val winter = (baseInterval * 1.8).toInt().coerceAtLeast(spring + 1)
        return SeasonalIntervals(spring = spring, summer = summer, autumn = autumn, winter = winter)
    }

    fun weekdayMask(days: Set<DayOfWeek>): Int =
        days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }
}

data class SeasonalIntervals(
    val spring: Int,
    val summer: Int,
    val autumn: Int,
    val winter: Int
)
