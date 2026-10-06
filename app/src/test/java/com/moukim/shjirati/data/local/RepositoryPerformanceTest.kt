package com.moukim.shjirati.data.local

import com.moukim.shjirati.domain.WateringCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.system.measureTimeMillis

class RepositoryPerformanceTest {

    @Test
    fun benchmark_batch_entity_mapping_and_due_filtering_20000_items() {
        val now = System.currentTimeMillis()
        val today = LocalDate.of(2026, 10, 5)

        val rawEntities = List(20000) { index ->
            PlantEntity(
                id = "entity_$index",
                name = "نبتة تجريبية $index",
                category = PlantCategory.entries[index % PlantCategory.entries.size],
                wateringIntervalDays = 2 + (index % 10),
                lastWateredAtEpochMillis = null,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        }

        var dueCount = 0
        val executionTimeMs = measureTimeMillis {
            val dueList = rawEntities.filter { WateringCalculator.isDueToday(it, today) }
            dueCount = dueList.size
        }

        println("Data Layer Benchmark: Batch processing 20,000 entities took ${executionTimeMs}ms (due count: $dueCount)")
        assertTrue(dueCount > 0)
        assertTrue("Batch filtering 20,000 items should take under 300ms, took: ${executionTimeMs}ms", executionTimeMs < 300)
    }

    @Test
    fun benchmark_image_uri_parsing_performance_50000_iterations() {
        val plant = PlantEntity(
            id = "p1",
            name = "نبتة",
            category = PlantCategory.TREE,
            imageUri = "/path/1.jpg|/path/2.jpg|/path/3.jpg|/path/4.jpg",
            createdAtEpochMillis = 0,
            updatedAtEpochMillis = 0
        )

        val executionTimeMs = measureTimeMillis {
            repeat(50000) {
                val list = plant.imageUrisList
                assertEquals(4, list.size)
            }
        }

        println("Data Layer Benchmark: Parsing 50,000 image URI lists took ${executionTimeMs}ms")
        assertTrue("Image URI parsing should take under 150ms, took: ${executionTimeMs}ms", executionTimeMs < 150)
    }
}
