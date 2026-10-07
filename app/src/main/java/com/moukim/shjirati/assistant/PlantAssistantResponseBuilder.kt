package com.moukim.shjirati.assistant

import com.moukim.shjirati.data.catalog.CatalogDateMode
import com.moukim.shjirati.data.catalog.PlantCatalogEntry
import com.moukim.shjirati.data.catalog.PlantArticle

sealed interface PlantAssistantResponse {
    data class Answer(val text: String) : PlantAssistantResponse
    data class NeedClarification(val text: String) : PlantAssistantResponse
}

class PlantAssistantResponseBuilder {
    fun build(decision: JebDecision, plant: PlantCatalogEntry?, article: PlantArticle? = null): PlantAssistantResponse {
        if (!decision.isConfident) return PlantAssistantResponse.NeedClarification(
            "لم أفهم السؤال بشكل كافٍ. حاول كتابة اسم النبتة وما تريد معرفته عنها."
        )
        if (plant == null) return PlantAssistantResponse.NeedClarification(
            "لم أتعرف على اسم النبتة في قاعدة بيانات شجيرتي."
        )
        return when (decision.intent) {
            JebIntent.PLANT_INFO -> buildInfo(plant, article)
            JebIntent.GERMINATION -> buildGermination(plant)
            JebIntent.HARVEST -> buildHarvest(plant)
            JebIntent.WATERING -> buildArticleField(plant, article?.watering, "الري")
            JebIntent.PLANTING -> buildPlanting(plant, article)
            else -> PlantAssistantResponse.NeedClarification(
                "هذه المعلومة غير متاحة حاليًا لهذه النبتة في قاعدة بيانات شجيرتي."
            )
        }
    }

    private fun buildInfo(plant: PlantCatalogEntry, article: PlantArticle?): PlantAssistantResponse {
        if (article == null) return PlantAssistantResponse.NeedClarification(
            "لا توجد مقالة معلوماتية مسجلة لـ" + plant.arabicName + " حاليًا."
        )
        val parts = listOf(
            article.description.takeIf { it.isNotBlank() },
            article.plantType.takeIf { it.isNotBlank() }?.let { "النوع: $it" },
            article.soil.takeIf { it.isNotBlank() }?.let { "التربة: $it" },
            article.sunlight.takeIf { it.isNotBlank() }?.let { "الإضاءة: $it" },
            article.watering.takeIf { it.isNotBlank() }?.let { "الري: $it" }
        ).filterNotNull()
        return if (parts.isEmpty()) PlantAssistantResponse.NeedClarification(
            "لا توجد معلومات كافية لـ" + plant.arabicName + " حاليًا."
        ) else PlantAssistantResponse.Answer(parts.joinToString("\n\n"))
    }

    private fun buildArticleField(plant: PlantCatalogEntry, value: String?, label: String): PlantAssistantResponse {
        return if (!value.isNullOrBlank()) PlantAssistantResponse.Answer(
            label + " لـ" + plant.arabicName + ": " + value
        ) else PlantAssistantResponse.NeedClarification(
            "لا توجد معلومة موثقة عن " + label + " لـ" + plant.arabicName + " حاليًا."
        )
    }

    private fun buildPlanting(plant: PlantCatalogEntry, article: PlantArticle?): PlantAssistantResponse {
        if (article == null) return PlantAssistantResponse.NeedClarification(
            "لا توجد معلومات زراعة مسجلة لـ" + plant.arabicName + " حاليًا."
        )
        val parts = listOf(
            article.plantingSeason.takeIf { it.isNotBlank() }?.let { "الموسم: $it" },
            article.plantingMethod.takeIf { it.isNotBlank() }?.let { "الطريقة: $it" },
            article.seedDepth.takeIf { it.isNotBlank() }?.let { "عمق البذور: $it" },
            article.spacing.takeIf { it.isNotBlank() }?.let { "المسافة: $it" }
        ).filterNotNull()
        return if (parts.isEmpty()) PlantAssistantResponse.NeedClarification(
            "لا توجد معلومات زراعة كافية لـ" + plant.arabicName + " حاليًا."
        ) else PlantAssistantResponse.Answer(parts.joinToString("\n"))
    }

    private fun buildGermination(plant: PlantCatalogEntry): PlantAssistantResponse {
        if (plant.dateMode != CatalogDateMode.GERMINATION) return PlantAssistantResponse.NeedClarification(
            "بيانات هذا النبات في شجيرتي لا تستخدم حاليًا تاريخ الإنبات."
        )
        val data = plant.growingData ?: return PlantAssistantResponse.NeedClarification(
            "لا توجد لدي بيانات موثقة عن مدة إنبات " + plant.arabicName + " حاليًا."
        )
        val min = data.germinationDaysMin
        val max = data.germinationDaysMax
        if (min == null || max == null) return PlantAssistantResponse.NeedClarification(
            "لا توجد لدي مدة إنبات موثقة كاملة لـ" + plant.arabicName + " حاليًا."
        )
        return PlantAssistantResponse.Answer(
            "تنبت " + plant.arabicName + " عادةً خلال " + min + " إلى " + max +
                " يومًا، وقد تختلف المدة حسب ظروف الزراعة."
        )
    }

    private fun buildHarvest(plant: PlantCatalogEntry): PlantAssistantResponse {
        val data = plant.growingData ?: return PlantAssistantResponse.NeedClarification(
            "لا توجد لدي بيانات موثقة عن مدة الحصاد لـ" + plant.arabicName + " حاليًا."
        )
        val min = data.maturityDaysMin
        val max = data.maturityDaysMax
        if (min == null || max == null) return PlantAssistantResponse.NeedClarification(
            "لا توجد لدي مدة حصاد موثقة كاملة لـ" + plant.arabicName + " حاليًا."
        )
        return PlantAssistantResponse.Answer(
            "تحتاج " + plant.arabicName + " عادةً إلى نحو " + min + " إلى " + max +
                " يومًا حتى النضج، وقد تختلف المدة حسب الصنف والظروف."
        )
    }
}
