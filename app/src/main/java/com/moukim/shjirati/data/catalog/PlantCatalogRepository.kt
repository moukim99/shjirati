package com.moukim.shjirati.data.catalog

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class PlantCatalogRepository(private val context: Context) {
    private val cachedEntries: List<PlantCatalogEntry> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val json = context.assets.open("plants/plant_catalog.json").bufferedReader().use { it.readText() }
        val plants = JSONObject(json).getJSONArray("plants")
        val growingData = loadGrowingData()
        buildList(plants.length()) {
            for (index in 0 until plants.length()) {
                val plant = plants.getJSONObject(index)
                add(plant.toEntry(growingData[plant.getString("id")]))
            }
        }
    }

    fun getAll(): List<PlantCatalogEntry> = cachedEntries

    fun search(query: String): List<PlantCatalogEntry> {
        val q = query.trim()
        if (q.isEmpty()) return getAll()
        return cachedEntries.filter {
            it.arabicName.contains(q, true) ||
                it.aliases.any { alias -> alias.contains(q, true) } ||
                it.scientificName.contains(q, true)
        }
    }

    fun imageAssetExists(entry: PlantCatalogEntry): Boolean =
        runCatching { context.assets.open(entry.imageAsset).use { true } }.getOrDefault(false)

    fun entriesWithMissingImages(): List<PlantCatalogEntry> =
        cachedEntries.filterNot(::imageAssetExists)

    private fun loadGrowingData(): Map<String, CatalogGrowingData> {
        val json = runCatching {
            context.assets.open("plants/growing_data.json").bufferedReader().use { it.readText() }
        }.getOrNull() ?: return emptyMap()

        val records = JSONObject(json).optJSONArray("plants") ?: return emptyMap()
        return buildMap(records.length()) {
            for (index in 0 until records.length()) {
                val record = records.getJSONObject(index)
                put(
                    record.getString("plantId"),
                    CatalogGrowingData(
                        germinationDaysMin = record.optIntOrNull("germinationDaysMin"),
                        germinationDaysMax = record.optIntOrNull("germinationDaysMax"),
                        maturityDaysMin = record.optIntOrNull("maturityDaysMin"),
                        maturityDaysMax = record.optIntOrNull("maturityDaysMax"),
                        source = record.optString("source").takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }

    private fun JSONObject.optIntOrNull(key: String): Int? =
        if (has(key) && !isNull(key)) optInt(key).takeIf { it > 0 } else null

    private fun JSONObject.toEntry(growingData: CatalogGrowingData?): PlantCatalogEntry {
        val a = optJSONArray("aliases") ?: JSONArray()
        return PlantCatalogEntry(
            id = getString("id"),
            arabicName = getString("arabicName"),
            aliases = buildList(a.length()) { for (i in 0 until a.length()) add(a.getString(i)) },
            scientificName = getString("scientificName"),
            category = CatalogPlantCategory.valueOf(getString("category")),
            dateMode = CatalogDateMode.valueOf(getString("dateMode")),
            imageAsset = getString("imageAsset"),
            growingData = growingData
        )
    }
}
