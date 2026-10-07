package com.moukim.shjirati.assistant

data class JebDecision(
    val intent: JebIntent,
    val confidence: Float,
) {
    val isConfident: Boolean
        get() = confidence >= DEFAULT_CONFIDENCE_THRESHOLD

    companion object {
        const val DEFAULT_CONFIDENCE_THRESHOLD = 0.75f
    }
}
