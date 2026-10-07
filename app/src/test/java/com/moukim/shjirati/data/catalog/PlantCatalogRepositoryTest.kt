package com.moukim.shjirati.data.catalog

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantCatalogRepositoryTest {
    private val repository = PlantCatalogRepository(
        ApplicationProvider.getApplicationContext()
    )

    @Test
    fun catalog_has_expected_starter_size() {
        assertEquals(200, repository.getAll().size)
    }

    @Test
    fun catalog_ids_are_unique() {
        val plants = repository.getAll()
        assertEquals(plants.size, plants.map { it.id }.toSet().size)
    }

    @Test
    fun catalog_scientific_names_are_present() {
        assertFalse(repository.getAll().any { it.scientificName.isBlank() })
    }

    @Test
    fun catalog_entries_have_valid_categories_and_date_modes() {
        repository.getAll().forEach { plant ->
            assertTrue(plant.arabicName.isNotBlank())
            assertTrue(plant.scientificName.isNotBlank())
            assertTrue(plant.imageAsset.startsWith("plants/"))
            when (plant.category) {
                CatalogPlantCategory.TREE -> assertEquals(CatalogDateMode.HARVEST, plant.dateMode)
                CatalogPlantCategory.VEGETABLE,
                CatalogPlantCategory.HERB -> assertEquals(CatalogDateMode.GERMINATION, plant.dateMode)
            }
        }
    }

    @Test
    fun Arabic_search_finds_common_alias() {
        val results = repository.search("بندورة")
        assertEquals("solanum_lycopersicum", results.single().id)
    }

    @Test
    fun image_asset_paths_are_local_and_well_formed() {
        repository.getAll().forEach { plant ->
            assertFalse(plant.imageAsset.startsWith("/"))
            assertFalse(plant.imageAsset.contains(".."))
            assertFalse(plant.imageAsset.isBlank())
        }
    }
}
