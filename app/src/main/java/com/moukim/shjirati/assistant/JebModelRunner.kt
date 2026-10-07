package com.moukim.shjirati.assistant

interface JebModelRunner {
    suspend fun decide(question: String): JebDecision
}
