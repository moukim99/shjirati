package com.moukim.shjirati.data.weather

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantWeatherProfileRepositoryTest {

    private val context: Context =
        ApplicationProvider.getApplicationContext()

    @Test
    fun profiles_cover_all_200_catalog_plants() {
        val catalogJson = context.assets.open("plants/plant_catalog.json")
            .bufferedReader().use { it.readText() }
        val catalogIds = Regex("""\"id\"\s*:\s*\"([^\"]+)\"""")
            .findAll(catalogJson)
            .map { it.groupValues[1] }
            .toList()

        val repository = PlantWeatherProfileRepository(context)
        val profiles = repository.getAll()

        assertEquals(200, catalogIds.size)
        assertEquals(200, profiles.size)
        assertEquals(200, profiles.map { it.plantId }.distinct().size)
        assertTrue(repository.missingPlantIds(catalogIds).isEmpty())
    }

    @Test
    fun tomato_profile_has_expected_advisory_bounds() {
        val profile = PlantWeatherProfileRepository(context)
            .getByPlantId("solanum_lycopersicum")

        assertTrue(profile != null)
        assertEquals(2.0, profile!!.frostRiskMinC, 0.0)
        assertEquals(35.0, profile.heatRiskMaxC, 0.0)
    }
}
