package com.moukim.shjirati.data.weather

import android.content.Context
import com.moukim.shjirati.data.local.WeatherDailyEntity
import com.moukim.shjirati.data.local.WeatherDao
import com.moukim.shjirati.data.local.DatabaseProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class WeatherRepository(
    private val weatherDao: WeatherDao,
    private val locationStore: GardenLocationStore,
    private val remote: OpenMeteoWeatherDataSource = OpenMeteoWeatherDataSource()
) {
    companion object {
        fun from(context: Context): WeatherRepository =
            WeatherRepository(
                weatherDao = DatabaseProvider.get(context).weatherDao(),
                locationStore = GardenLocationStore(context)
            )
    }

    fun currentLocation(): WeatherLocation? = locationStore.getLocation()

    fun observe(): Flow<List<WeatherDaily>> {
        val location = requireLocation()
        return observe(location)
    }

    fun observe(location: WeatherLocation): Flow<List<WeatherDaily>> =
        weatherDao.observeDaily(location.latitude, location.longitude)
            .map { it.map(WeatherDailyEntity::toDomain) }

    suspend fun refresh(): WeatherResult {
        val location = requireLocation()
        return refresh(location)
    }

    suspend fun refresh(location: WeatherLocation): WeatherResult = try {
        val remoteDays = remote.fetch(location)
        weatherDao.upsertDaily(remoteDays.map { it.toEntity(location) })
        weatherDao.deleteOlderThan(
            location.latitude,
            location.longitude,
            LocalDate.now().minusDays(30).toEpochDay()
        )
        WeatherResult.Fresh(remoteDays)
    } catch (error: Exception) {
        WeatherResult.Failure(
            getCached(location),
            error.message ?: "تعذر تحديث بيانات الطقس"
        )
    }

    suspend fun getCached(): List<WeatherDaily> =
        getCached(requireLocation())

    suspend fun getCached(location: WeatherLocation): List<WeatherDaily> =
        weatherDao.getDaily(
            location.latitude,
            location.longitude,
            LocalDate.now().minusDays(30).toEpochDay(),
            LocalDate.now().plusDays(14).toEpochDay()
        ).map(WeatherDailyEntity::toDomain)

    private fun requireLocation(): WeatherLocation =
        locationStore.getLocation()
            ?: throw IllegalStateException("لم يتم تحديد موقع الحديقة بعد.")
}

private fun WeatherDaily.toEntity(l: WeatherLocation) =
    WeatherDailyEntity(
        dateEpochDay,
        l.latitude,
        l.longitude,
        temperatureMinC,
        temperatureMaxC,
        precipitationMm,
        precipitationProbabilityPercent,
        humidityMeanPercent,
        windSpeedMaxKmh,
        frostRisk,
        heatRisk,
        downloadedAtEpochMillis
    )

private fun WeatherDailyEntity.toDomain() =
    WeatherDaily(
        dateEpochDay,
        temperatureMinC,
        temperatureMaxC,
        precipitationMm,
        precipitationProbabilityPercent,
        humidityMeanPercent,
        windSpeedMaxKmh,
        frostRisk,
        heatRisk,
        downloadedAtEpochMillis
    )
