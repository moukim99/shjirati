package com.moukim.shjirati.data.weather

import android.content.Context
import org.json.JSONObject

class PlantWeatherProfileRepository(private val context: Context) {
    private val profiles: Map<String, PlantWeatherProfile> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val json = context.assets.open("plants/plant_weather_profiles.json")
            .bufferedReader().use { it.readText() }
        val records = JSONObject(json).optJSONArray("profiles") ?: return@lazy emptyMap()
        buildMap(records.length()) {
            for (i in 0 until records.length()) {
                val r = records.getJSONObject(i)
                val p = PlantWeatherProfile(
                    plantId = r.getString("plantId"),
                    frostRiskMinC = r.getDouble("frostRiskMinC"),
                    heatRiskMaxC = r.getDouble("heatRiskMaxC"),
                    source = r.optString("source")
                )
                put(p.plantId, p)
            }
        }
    }

    fun getByPlantId(plantId: String): PlantWeatherProfile? = profiles[plantId]

    fun getAll(): List<PlantWeatherProfile> = profiles.values.toList()

    fun missingPlantIds(catalog: List<String>): List<String> =
        catalog.filterNot(profiles::containsKey)
}
