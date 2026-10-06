package com.moukim.shjirati.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class ShjiratiRoomPerformanceTest {

    private lateinit var database: ShjiratiDatabase
    private lateinit var dao: ShjiratiDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShjiratiDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.dao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun benchmark_room_bulk_insert_1000_plants() = runBlocking {
        val now = System.currentTimeMillis()
        val plants = List(1000) { index ->
            PlantEntity(
                id = "room_plant_$index",
                name = "شجرة زيتون $index",
                category = PlantCategory.TREE,
                location = "حديقة $index",
                wateringIntervalDays = 3 + (index % 5),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        }

        val executionTimeMs = measureTimeMillis {
            for (plant in plants) {
                dao.upsertPlant(plant)
            }
        }

        val count = dao.observePlants().first().size
        assertEquals(1000, count)

        println("Room Benchmark: Bulk insert of 1,000 plants into Room DB took ${executionTimeMs}ms")
        assertTrue("Bulk inserting 1,000 plants into Room DB should execute in under 2500ms, took: ${executionTimeMs}ms", executionTimeMs < 2500)
    }

    @Test
    fun benchmark_room_query_and_sorting_1000_plants() = runBlocking {
        val now = System.currentTimeMillis()
        val plants = List(1000) { index ->
            PlantEntity(
                id = "plant_query_$index",
                name = "نبتة رقم $index",
                category = PlantCategory.entries[index % PlantCategory.entries.size],
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        }

        for (plant in plants) {
            dao.upsertPlant(plant)
        }

        val queryTimeMs = measureTimeMillis {
            val result = dao.observePlants().first()
            assertEquals(1000, result.size)
        }

        println("Room Benchmark: Querying and sorting 1,000 plants from Room DB took ${queryTimeMs}ms")
        assertTrue("Querying 1,000 plants from Room DB should execute in under 300ms, took: ${queryTimeMs}ms", queryTimeMs < 300)
    }

    @Test
    fun benchmark_room_watering_log_inserts_and_indexed_query() = runBlocking {
        val now = System.currentTimeMillis()
        val plantId = "indexed_plant_1"
        
        dao.upsertPlant(
            PlantEntity(
                id = plantId,
                name = "شجرة ليمون",
                category = PlantCategory.TREE,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )

        val logs = List(2000) { index ->
            WateringLogEntity(
                id = "log_$index",
                plantId = plantId,
                wateredAtEpochMillis = now - (index * 86_400_000L),
                amountMl = 500,
                durationMinutes = 5
            )
        }

        for (log in logs) {
            dao.insertWateringLog(log)
        }

        val queryTimeMs = measureTimeMillis {
            val history = dao.observeWateringHistory(plantId).first()
            assertEquals(2000, history.size)
        }

        println("Room Benchmark: Querying 2,000 indexed watering logs from Room DB took ${queryTimeMs}ms")
        assertTrue("Querying 2,000 indexed logs should take under 100ms, took: ${queryTimeMs}ms", queryTimeMs < 100)
    }
}
