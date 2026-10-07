package com.moukim.shjirati.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JebDecisionSchemaTest {
    @Test
    fun schema_contains_expected_shjirati_intents() {
        assertEquals(
            listOf(
                JebIntent.PLANT_INFO,
                JebIntent.GERMINATION,
                JebIntent.HARVEST,
                JebIntent.WATERING,
                JebIntent.PLANTING,
                JebIntent.SEARCH_PLANT,
                JebIntent.UNKNOWN,
            ),
            JebDecisionSchema.options.map(JebOption::intent),
        )
    }

    @Test
    fun prompt_contains_question_and_one_mask_per_option() {
        val parts = JebDecisionSchema.promptParts("متى تنبت الطماطم؟")
        assertTrue(parts.contains("متى تنبت الطماطم؟"))
        assertEquals(JebDecisionSchema.options.size, parts.count { it == "[MASK]" })
    }

    @Test
    fun low_confidence_decision_is_not_accepted() {
        assertTrue(!JebDecision(JebIntent.GERMINATION, 0.74f).isConfident)
        assertTrue(JebDecision(JebIntent.GERMINATION, 0.75f).isConfident)
    }
}
