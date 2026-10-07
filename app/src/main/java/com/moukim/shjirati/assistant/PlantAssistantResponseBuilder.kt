package com.moukim.shjirati.assistant

import com.moukim.shjirati.data.catalog.CatalogDateMode
import com.moukim.shjirati.data.catalog.PlantCatalogEntry

sealed interface PlantAssistantResponse {
    data class Answer(val text: String) : PlantAssistantResponse
    data class NeedClarification(val text: String) : PlantAssistantResponse
}

class PlantAssistantResponseBuilder {
    fun build(decision: JebDecision, plant: PlantCatalogEntry?): PlantAssistantResponse {
        if (!decision.isConfident) return PlantAssistantResponse.NeedClarification(
            "لم أفهم السؤال بشكل كافٍ. حاول كتابة اسم النبتة وما تريد معرفته عنها."
        )
        if (plant == null) return PlantAssistantResponse.NeedClarification(
            "لم أتعرف على اسم النبتة في قاعدة بيانات شجيرتي."
        )
        return when (decision.intent) {
            JebIntent.GERMINATION -> buildGermination(plant)
            JebIntent.HARVEST -> buildHarvest(plant)
            else -> PlantAssistantResponse.NeedClarification(
                "هذه المعلومة غير متاحة حاليًا لهذه النبتة في قاعدة بيانات شجيرتي."
            )
        }
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
