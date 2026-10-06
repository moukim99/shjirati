package com.moukim.shjirati.domain

import com.moukim.shjirati.data.local.PlantCategory
import com.moukim.shjirati.data.local.PlantEntity
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import kotlin.system.measureTimeMillis

class WateringCalculatorPerformanceTest {

    private fun generateMockPlants(count: Int): List<PlantEntity> {
        val now = System.currentTimeMillis()
        val baseDate = LocalDate.of(2026, 10, 5)
        val zoneId = ZoneId.systemDefault()

        return List(count) { index ->
            val lastWateredDaysAgo = (index % 14).toLong()
            val lastMillis = baseDate.minusDays(lastWateredDaysAgo)
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli()

            PlantEntity(
                id = "plant_$index",
                name = "نبتة $index",
                category = PlantCategory.entries[index % PlantCategory.entries.size],
                wateringIntervalDays = if ((index % 2) == 0) (3 + (index % 7)) else null,
                wateringDaysMask = if ((index % 2) != 0) (1 shl (index % 7)) else 0,
                wateringHour = 8 + (index % 12),
                wateringMinute = (index % 60),
                seasonalScheduleEnabled = (index % 3) == 0,
                springIntervalDays = 5,
                summerIntervalDays = 3,
                autumnIntervalDays = 7,
                winterIntervalDays = 10,
                lastWateredAtEpochMillis = lastMillis,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            )
        }
    }

    @Test
    fun benchmark_isDueToday_for_10000_plants() {
        val plants = generateMockPlants(10000)
        val testDate = LocalDate.of(2026, 10, 5)

        // Warmup
        plants.take(500).forEach { WateringCalculator.isDueToday(it, testDate) }

        // Execution time measurement
        val executionTimeMs = measureTimeMillis {
            var dueCount = 0
            for (plant in plants) {
                if (WateringCalculator.isDueToday(plant, testDate)) {
                    dueCount++
                }
            }
            assertTrue("Expected some plants to be due", dueCount > 0)
        }

        println("Speed Test: 10,000 plant schedule calculations took ${executionTimeMs}ms")
        assertTrue("Calculation for 10,000 plants should execute in under 500ms, took: ${executionTimeMs}ms", executionTimeMs < 500)
    }

    @Test
    fun benchmark_seasonal_interval_lookup_50000_iterations() {
        val plant = generateMockPlants(1).first().copy(
            seasonalScheduleEnabled = true,
            springIntervalDays = 4,
            summerIntervalDays = 2,
            autumnIntervalDays = 6,
            winterIntervalDays = 9,
        )

        val dates = listOf(
            LocalDate.of(2026, 4, 15), // Spring
            LocalDate.of(2026, 7, 15), // Summer
            LocalDate.of(2026, 10, 15), // Autumn
            LocalDate.of(2026, 1, 15)  // Winter
        )

        // Warmup
        repeat(1000) { WateringCalculator.intervalFor(plant, dates[it % dates.size]) }

        val executionTimeMs = measureTimeMillis {
            repeat(50000) { i ->
                val interval = WateringCalculator.intervalFor(plant, dates[i % dates.size])
                assertTrue(interval != null)
            }
        }

        println("Speed Test: 50,000 seasonal interval calculations took ${executionTimeMs}ms")
        assertTrue("50,000 seasonal calculations should execute in under 300ms, took: ${executionTimeMs}ms", executionTimeMs < 300)
    }

    @Test
    fun benchmark_weekday_mask_generation_100000_iterations() {
        val daysSample = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SUNDAY)

        val executionTimeMs = measureTimeMillis {
            repeat(100000) {
                val mask = WateringCalculator.weekdayMask(daysSample)
                assertTrue(mask > 0)
            }
        }

        println("Speed Test: 100,000 weekday mask calculations took ${executionTimeMs}ms")
        assertTrue("100,000 mask calculations should execute in under 200ms, took: ${executionTimeMs}ms", executionTimeMs < 200)
    }

    @Test
    fun benchmark_sorting_and_filtering_5000_plants() {
        val plants = generateMockPlants(5000)
        val today = LocalDate.of(2026, 10, 5)

        val executionTimeMs = measureTimeMillis {
            val duePlants = plants.asSequence()
                .filter { WateringCalculator.isDueToday(it, today) }
                .sortedBy { plant -> plant.name }
                .toList()
            assertTrue("Expected filtered list to not be empty", duePlants.isNotEmpty())
        }

        println("Speed Test: Filtering and sorting 5,000 plants took ${executionTimeMs}ms")
        assertTrue("Filtering & sorting 5,000 plants should take under 300ms, took: ${executionTimeMs}ms", executionTimeMs < 300)
    }
}
