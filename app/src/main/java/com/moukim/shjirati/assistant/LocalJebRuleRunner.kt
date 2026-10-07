package com.moukim.shjirati.assistant

/**
 * Deterministic offline fallback used when no ONNX model is available.
 * It understands the small intent vocabulary needed to query the local plant knowledge base.
 */
class LocalJebRuleRunner : JebModelRunner {
    override suspend fun decide(question: String): JebDecision {
        val q = question.trim().lowercase()
        if (q.isBlank()) return JebDecision(JebIntent.UNKNOWN, 0f)

        val intent = when {
            listOf("كم يوم", "مدة الإنبات", "الإنبات", "تنبت", "متى تنبت").any(q::contains) ->
                JebIntent.GERMINATION
            listOf("متى أحصد", "متى احصد", "الحصاد", "تنضج", "النضج").any(q::contains) ->
                JebIntent.HARVEST
            listOf("كيف أسقي", "كيف اسقي", "السقي", "الري", "كم مرة أسقي", "كم مره اسقي").any(q::contains) ->
                JebIntent.WATERING
            listOf("كيف أزرع", "كيف ازرع", "الزراعة", "الزرع", "متى أزرع", "متى ازرع").any(q::contains) ->
                JebIntent.PLANTING
            listOf("ما هي", "ما هو", "معلومات", "أخبرني", "اخبرني", "عن ").any(q::contains) ->
                JebIntent.PLANT_INFO
            else -> JebIntent.PLANT_INFO
        }
        return JebDecision(intent, if (intent == JebIntent.PLANT_INFO) 0.78f else 0.92f)
    }
}
