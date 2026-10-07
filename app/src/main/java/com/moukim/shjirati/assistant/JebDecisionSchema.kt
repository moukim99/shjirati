package com.moukim.shjirati.assistant

data class JebOption(
    val intent: JebIntent,
    val description: String,
)

object JebDecisionSchema {
    val options: List<JebOption> = listOf(
        JebOption(JebIntent.PLANT_INFO, "معلومات عامة عن النبات"),
        JebOption(JebIntent.GERMINATION, "موعد أو مدة إنبات النبات"),
        JebOption(JebIntent.HARVEST, "موعد أو مدة الحصاد"),
        JebOption(JebIntent.WATERING, "السقي والري"),
        JebOption(JebIntent.PLANTING, "الزراعة والغرس"),
        JebOption(JebIntent.SEARCH_PLANT, "البحث عن نبات"),
        JebOption(JebIntent.UNKNOWN, "لا ينتمي السؤال إلى المعلومات المتاحة"),
    )

    fun promptParts(question: String): List<String> {
        val cleanQuestion = question.trim()
        require(cleanQuestion.isNotEmpty()) { "Question must not be blank" }

        return buildList {
            add("[CLS]")
            add(cleanQuestion)
            add("[SEP]")
            add("ما نوع الطلب؟")
            options.forEach { option ->
                add("[MASK]")
                add(option.intent.name + ": " + option.description)
            }
        }
    }
}
