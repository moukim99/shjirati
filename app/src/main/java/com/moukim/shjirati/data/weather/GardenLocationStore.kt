package com.moukim.shjirati.data.weather

import android.content.Context

class GardenLocationStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLocation(): WeatherLocation? {
        if (!preferences.contains(KEY_LATITUDE) || !preferences.contains(KEY_LONGITUDE)) return null
        return WeatherLocation(
            latitude = preferences.getFloat(KEY_LATITUDE, 0f).toDouble(),
            longitude = preferences.getFloat(KEY_LONGITUDE, 0f).toDouble()
        )
    }

    fun saveLocation(location: WeatherLocation) {
        preferences.edit()
            .putFloat(KEY_LATITUDE, location.latitude.toFloat())
            .putFloat(KEY_LONGITUDE, location.longitude.toFloat())
            .apply()
    }

    fun clear() {
        preferences.edit()
            .remove(KEY_LATITUDE)
            .remove(KEY_LONGITUDE)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "garden_location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }
}
