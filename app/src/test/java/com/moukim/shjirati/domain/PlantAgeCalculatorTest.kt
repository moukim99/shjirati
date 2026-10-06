package com.moukim.shjirati.domain

import com.moukim.shjirati.data.local.PlantCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PlantAgeCalculatorTest {

    private fun epochForDate(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    @Test
    fun returns_null_when_plantedAt_is_null() {
        assertNull(PlantAgeCalculator.calculateAge(null, PlantCategory.TREE))
    }

    @Test
    fun calculates_tree_age_years_and_months() {
        val today = LocalDate.of(2026, 9, 15)
        // Planted 4 years and 6 months ago: March 15, 2022
        val plantedAt = epochForDate(2022, 3, 15)

        val age = PlantAgeCalculator.calculateAge(plantedAt, PlantCategory.TREE, today)
        assertEquals("4 سنوات و6 أشهر", age)
    }

    @Test
    fun calculates_vegetable_age_months_and_days() {
        val today = LocalDate.of(2026, 9, 15)
        // Planted 3 months and 12 days ago: June 3, 2026
        val plantedAt = epochForDate(2026, 6, 3)

        val age = PlantAgeCalculator.calculateAge(plantedAt, PlantCategory.VEGETABLE, today)
        assertEquals("3 أشهر و12 يومًا", age)
    }

    @Test
    fun calculates_small_plant_days() {
        val today = LocalDate.of(2026, 9, 15)
        // Planted 15 days ago
        val plantedAt = epochForDate(2026, 8, 31)

        val age = PlantAgeCalculator.calculateAge(plantedAt, PlantCategory.VEGETABLE, today)
        assertEquals("15 يومًا", age)
    }
}
