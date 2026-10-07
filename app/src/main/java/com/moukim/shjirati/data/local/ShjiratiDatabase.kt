package com.moukim.shjirati.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class ShjiratiConverters {
    @TypeConverter
    fun categoryToString(value: PlantCategory): String = value.name

    @TypeConverter
    fun stringToCategory(value: String): PlantCategory = try {
        PlantCategory.valueOf(value)
    } catch (e: Exception) {
        if (value == "VEGETABLE") PlantCategory.VEGETABLE else PlantCategory.TREE
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS weather_daily (dateEpochDay INTEGER NOT NULL, latitude REAL NOT NULL, longitude REAL NOT NULL, temperatureMinC REAL, temperatureMaxC REAL, precipitationMm REAL, precipitationProbabilityPercent INTEGER, humidityMeanPercent REAL, windSpeedMaxKmh REAL, frostRisk INTEGER NOT NULL, heatRisk INTEGER NOT NULL, downloadedAtEpochMillis INTEGER NOT NULL, PRIMARY KEY(dateEpochDay, latitude, longitude))""")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_weather_daily_dateEpochDay ON weather_daily(dateEpochDay)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_weather_daily_downloadedAtEpochMillis ON weather_daily(downloadedAtEpochMillis)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE plants ADD COLUMN expectedDateEpochMillis INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE plants ADD COLUMN isFruitBearing INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE plants ADD COLUMN icon TEXT DEFAULT NULL")
        db.execSQL("UPDATE plants SET category = 'TREE' WHERE category NOT IN ('TREE', 'VEGETABLE')")
    }
}

@Database(
    entities = [PlantEntity::class, WateringLogEntity::class, WeatherDailyEntity::class],
    version = 4,
    exportSchema = true
)
@androidx.room.TypeConverters(ShjiratiConverters::class)
abstract class ShjiratiDatabase : RoomDatabase() {
    abstract fun dao(): ShjiratiDao
    abstract fun weatherDao(): WeatherDao
}
