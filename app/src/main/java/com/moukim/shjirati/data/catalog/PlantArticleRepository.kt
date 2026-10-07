package com.moukim.shjirati.data.catalog

import android.content.Context
import org.json.JSONObject

class PlantArticleRepository(private val context: Context) {

    private val cachedArticles: Map<String, PlantArticle> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val json = context.assets
            .open("plants/plant_articles.json")
            .bufferedReader()
            .use { it.readText() }

        val records = JSONObject(json).optJSONArray("articles")
            ?: return@lazy emptyMap()

        buildMap(records.length()) {
            for (index in 0 until records.length()) {
                val record = records.getJSONObject(index)
                val article = record.toPlantArticle()
                put(article.plantId, article)
            }
        }
    }

    fun getAll(): List<PlantArticle> = cachedArticles.values.toList()

    fun getByPlantId(plantId: String): PlantArticle? =
        cachedArticles[plantId]

    fun hasArticle(plantId: String): Boolean =
        cachedArticles.containsKey(plantId)

    fun missingArticlePlantIds(catalog: PlantCatalogRepository): List<String> =
        catalog.getAll().map { it.id }.filterNot(::hasArticle)

    fun validateAgainstCatalog(catalog: PlantCatalogRepository): PlantArticleValidationResult {
        val catalogIds = catalog.getAll().map { it.id }.toSet()
        val articleIds = cachedArticles.keys

        val unknownArticleIds = articleIds.filterNot(catalogIds::contains).sorted()
        val missingArticleIds = catalogIds.filterNot(articleIds::contains).sorted()

        return PlantArticleValidationResult(
            catalogCount = catalogIds.size,
            articleCount = articleIds.size,
            unknownArticleIds = unknownArticleIds,
            missingArticleIds = missingArticleIds
        )
    }

    private fun JSONObject.toPlantArticle(): PlantArticle =
        PlantArticle(
            plantId = getString("plantId"),
            description = optString("description"),
            origin = optString("origin"),
            plantType = optString("plantType"),
            propagation = optString("propagation"),
            soil = optString("soil"),
            soilPh = optString("soilPh"),
            sunlight = optString("sunlight"),
            watering = optString("watering"),
            temperature = optString("temperature"),
            frostTolerance = optString("frostTolerance"),
            heatTolerance = optString("heatTolerance"),
            plantingSeason = optString("plantingSeason"),
            plantingMethod = optString("plantingMethod"),
            seedDepth = optString("seedDepth"),
            spacing = optString("spacing"),
            germination = optString("germination"),
            growthHarvest = optString("growthHarvest"),
            fertilization = optString("fertilization"),
            pestsDiseases = optString("pestsDiseases"),
            notes = optString("notes"),
            sourceNotes = optString("sourceNotes")
        )
}

data class PlantArticleValidationResult(
    val catalogCount: Int,
    val articleCount: Int,
    val unknownArticleIds: List<String>,
    val missingArticleIds: List<String>
) {
    val isValid: Boolean
        get() = unknownArticleIds.isEmpty() &&
            articleCount == catalogCount
}
