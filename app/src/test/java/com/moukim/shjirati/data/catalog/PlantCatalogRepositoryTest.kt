package com.moukim.shjirati.data.catalog

import androidx.test.core.app.ApplicationProvider
import com.moukim.shjirati.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PlantCatalogRepositoryTest {
    private val repository = PlantCatalogRepository(
        ApplicationProvider.getApplicationContext()
    )

    @Test
    fun catalog_has_expected_starter_size() {
        assertEquals(100, repository.getAll().size)
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