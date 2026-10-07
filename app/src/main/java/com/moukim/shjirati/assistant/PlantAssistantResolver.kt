package com.moukim.shjirati.assistant

import com.moukim.shjirati.data.catalog.PlantCatalogEntry
import com.moukim.shjirati.data.catalog.PlantCatalogRepository

class PlantAssistantResolver(
    private val catalogRepository: PlantCatalogRepository,
) {
    fun resolvePlant(question: String): PlantCatalogEntry? {
        val clean = question.trim()
        if (clean.isEmpty()) return null
        return catalogRepository.search(clean).firstOrNull()
            ?: catalogRepository.getAll().firstOrNull { entry ->
                entry.aliases.any { alias -> clean.contains(alias, ignoreCase = true) }
            }
    }
}
