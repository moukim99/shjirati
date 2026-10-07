package com.moukim.shjirati.data.catalog

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class PlantCatalogRepository(private val context: Context) {
    fun getAll(): List<PlantCatalogEntry> {
        val json = context.assets.open("plants/plant_catalog.json").bufferedReader().use { it.readText() }
        val plants = JSONObject(json).getJSONArray("plants")
        return buildList(plants.length()) {
            for (index in 0 until plants.length()) add(plants.getJSONObject(index).toEntry())
        }
    }

    fun search(query: String): List<PlantCatalogEntry> {
        val q = query.trim()
        if (q.isEmpty()) return getAll()
        return getAll().filter {
            it.arabicName.contains(q, true) ||
                it.aliases.any { alias -> alias.contains(q, true) } ||
                it.scientificName.contains(q, true)
        }
    }

    private fun JSONObject.toEntry(): PlantCatalogEntry {
        val a = optJSONArray("aliases") ?: JSONArray()
        return PlantCatalogEntry(
            id = getString("id"),
            arabicName = getString("arabicName"),
            aliases = buildList(a.length()) { for (i in 0 until a.length()) add(a.getString(i)) },
            scientificName = getString("scientificName"),
            category = CatalogPlantCategory.valueOf(getString("category")),
            dateMode = CatalogDateMode.valueOf(getString("dateMode")),
            imageAsset = getString("imageAsset")
        )
    }
}
