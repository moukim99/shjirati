package com.moukim.shjirati.assistant

import com.moukim.shjirati.data.catalog.CatalogDateMode
import com.moukim.shjirati.data.catalog.CatalogGrowingData
import com.moukim.shjirati.data.catalog.CatalogPlantCategory
import com.moukim.shjirati.data.catalog.PlantCatalogEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class PlantAssistantResponseBuilderTest {
    private val tomato = PlantCatalogEntry(
        id = "solanum_lycopersicum",
        arabicName = "طماطم",
        aliases = listOf("بندورة"),
        scientificName = "Solanum lycopersicum",
        category = CatalogPlantCategory.VEGETABLE,
        dateMode = CatalogDateMode.GERMINATION,
        imageAsset = "plants/solanum_lycopersicum.webp",
        growingData = CatalogGrowingData(germinationDaysMin = 6, germinationDaysMax = 12),
    )

    @Test
    fun germination_answer_uses_local_growing_data_only() {
        val result = PlantAssistantResponseBuilder().build(
            JebDecision(JebIntent.GERMINATION, 0.9f), tomato
        )
        assertEquals(
            PlantAssistantResponse.Answer(
                "تنبت طماطم عادةً خلال 6 إلى 12 يومًا، وقد تختلف المدة حسب ظروف الزراعة."
            ),
            result,
        )
    }

    @Test
    fun low_confidence_never_returns_fact() {
        val result = PlantAssistantResponseBuilder().build(
            JebDecision(JebIntent.GERMINATION, 0.74f), tomato
        )
        assertEquals(
            PlantAssistantResponse.NeedClarification(
                "لم أفهم السؤال بشكل كافٍ. حاول كتابة اسم النبتة وما تريد معرفته عنها."
            ),
            result,
        )
    }
}
