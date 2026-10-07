package com.moukim.shjirati.data.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.LocalDate

class OpenMeteoWeatherDataSource {
    suspend fun fetch(location: WeatherLocation): List<WeatherDaily> = withContext(Dispatchers.IO) {
        val url = URL("https://api.open-meteo.com/v1/forecast?latitude=" + location.latitude +
            "&longitude=" + location.longitude +
            "&daily=temperature_2m_min,temperature_2m_max,precipitation_sum,precipitation_probability_max,relative_humidity_2m_mean,wind_speed_10m_max" +
            "&forecast_days=14&timezone=auto")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"; connectTimeout = 10_000; readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            check(connection.responseCode in 200..299) { "Weather service returned HTTP " + connection.responseCode }
            parse(connection.inputStream.bufferedReader().use { it.readText() })
        } finally { connection.disconnect() }
    }

    private fun parse(jsonText: String): List<WeatherDaily> {
        val daily = JSONObject(jsonText).getJSONObject("daily")
        val dates = daily.getJSONArray("time")
        val min = daily.optJSONArray("temperature_2m_min")
        val max = daily.optJSONArray("temperature_2m_max")
        val rain = daily.optJSONArray("precipitation_sum")
        val probability = daily.optJSONArray("precipitation_probability_max")
        val humidity = daily.optJSONArray("relative_humidity_2m_mean")
        val wind = daily.optJSONArray("wind_speed_10m_max")
        val downloadedAt = Instant.now().toEpochMilli()
        return buildList(dates.length()) {
            for (i in 0 until dates.length()) {
                val minC = min?.optDoubleOrNull(i); val maxC = max?.optDoubleOrNull(i)
                add(WeatherDaily(LocalDate.parse(dates.getString(i)).toEpochDay(), minC, maxC,
                    rain?.optDoubleOrNull(i), probability?.optIntOrNull(i), humidity?.optDoubleOrNull(i),
                    wind?.optDoubleOrNull(i), (minC ?: Double.POSITIVE_INFINITY) <= 2.0,
                    (maxC ?: Double.NEGATIVE_INFINITY) >= 38.0, downloadedAt))
            }
        }
    }
    private fun org.json.JSONArray.optDoubleOrNull(i: Int): Double? = if (isNull(i)) null else optDouble(i)
    private fun org.json.JSONArray.optIntOrNull(i: Int): Int? = if (isNull(i)) null else optInt(i)
}
