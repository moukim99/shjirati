package com.moukim.shjirati.data.catalog

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantArticleRepositoryTest {

    private val context: Context =
        ApplicationProvider.getApplicationContext()

    @Test
    fun articles_load_and_match_catalog_ids() {
        val catalog = PlantCatalogRepository(context)
        val articles = PlantArticleRepository(context)

        val validation = articles.validateAgainstCatalog(catalog)

        assertEquals(200, validation.catalogCount)
        assertTrue(validation.unknownArticleIds.isEmpty())
        assertEquals(180, validation.articleCount)
        assertEquals(20, validation.missingArticleIds.size)
    }

    @Test
    fun tomato_article_is_available() {
        val repository = PlantArticleRepository(context)
        val article = repository.getByPlantId("solanum_lycopersicum")

        assertNotNull(article)
        assertTrue(article!!.description.isNotBlank())
        assertTrue(article.soil.isNotBlank())
        assertTrue(article.frostTolerance.isNotBlank())
    }
}
