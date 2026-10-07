package com.moukim.shjirati.assistant

import com.moukim.shjirati.data.catalog.CatalogGrowingData
import com.moukim.shjirati.data.catalog.CatalogPlantCategory
import com.moukim.shjirati.data.catalog.PlantCatalogEntry
import com.moukim.shjirati.data.catalog.PlantArticle
import kotlin.test.Test
import kotlin.test.assertTrue

class LocalJebRuleRunnerTest {
    @Test
    fun classifies_common_arabic_questions() = kotlinx.coroutines.test.runTest {
        val runner = LocalJebRuleRunner()
        assertTrue(runner.decide("كيف أسقي الطماطم؟").intent == JebIntent.WATERING)
        assertTrue(runner.decide("متى تنبت الطماطم؟").intent == JebIntent.GERMINATION)
        assertTrue(runner.decide("متى أحصد الطماطم؟").intent == JebIntent.HARVEST)
        assertTrue(runner.decide("ما هي الطماطم؟").intent == JebIntent.PLANT_INFO)
    }
}
