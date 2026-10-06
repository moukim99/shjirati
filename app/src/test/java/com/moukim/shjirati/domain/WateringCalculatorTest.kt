package com.moukim.shjirati.domain

import com.moukim.shjirati.data.local.PlantCategory
import com.moukim.shjirati.data.local.PlantEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class WateringCalculatorTest {
    private fun plant(
        interval: Int? = 3,
        daysMask: Int = 0,
        lastWatered: LocalDate? = null
    ): PlantEntity {
        val now = System.currentTimeMillis()
        val lastMillis = lastWatered?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        return PlantEntity(
            id = "test",
            name = "نبتة",
            category = PlantCategory.TREE,
            wateringIntervalDays = interval,
            wateringDaysMask = daysMask,
            lastWateredAtEpochMillis = lastMillis,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )
    }

    @Test
    fun interval_is_due_when_no_previous_watering_exists() {
        assertTrue(WateringCalculator.isDueToday(plant(), LocalDate.of(2026, 10, 5)))
    }

    @Test
    fun interval_is_not_due_before_interval_is_complete() {
        val today = LocalDate.of(2026, 10, 5)
        assertFalse(WateringCalculator.isDueToday(plant(lastWatered = today.minusDays(2)), today))
    }

    @Test
    fun interval_is_due_when_interval_is_complete() {
        val today = LocalDate.of(2026, 10, 5)
        assertTrue(WateringCalculator.isDueToday(plant(lastWatered = today.minusDays(3)), today))
    }

    @Test
    fun weekday_schedule_is_due_on_selected_day() {
        val today = LocalDate.of(2026, 10, 5) // Monday
        val mondayMask = 1 shl (today.dayOfWeek.value - 1)
        assertTrue(WateringCalculator.isDueToday(plant(interval = null, daysMask = mondayMask), today))
    }

    @Test
    fun seasonal_interval_is_used_for_current_season() {
        val today = LocalDate.of(2026, 7, 10)
        val p = plant(interval = 10, lastWatered = today.minusDays(5)).copy(
            seasonalScheduleEnabled = true,
            summerIntervalDays = 5
        )
        assertTrue(WateringCalculator.isDueToday(p, today))
    }

    @Test
    fun seasonal_interval_falls_back_to_base_when_missing() {
        val today = LocalDate.of(2026, 7, 10)
        val p = plant(interval = 7, lastWatered = today.minusDays(6)).copy(
            seasonalScheduleEnabled = true,
            summerIntervalDays = null
        )
        assertFalse(WateringCalculator.isDueToday(p, today))
    }

    @Test
    fun watered_today_is_not_due_again() {
        val today = LocalDate.of(2026, 10, 5)
        val mondayMask = 1 shl (today.dayOfWeek.value - 1)
        assertFalse(
            WateringCalculator.isDueToday(
                plant(interval = null, daysMask = mondayMask, lastWatered = today),
                today
            )
        )
    }

    @Test
    fun default_seasonal_intervals_are_calculated_correctly() {
        val defaults = WateringCalculator.defaultSeasonalIntervals(10)
        org.junit.Assert.assertEquals(10, defaults.spring)
        org.junit.Assert.assertEquals(6, defaults.summer)
        org.junit.Assert.assertEquals(12, defaults.autumn)
        org.junit.Assert.assertEquals(18, defaults.winter)
    }
}
