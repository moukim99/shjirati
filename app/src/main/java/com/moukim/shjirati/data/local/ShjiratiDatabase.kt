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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE plants ADD COLUMN expectedDateEpochMillis INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE plants ADD COLUMN isFruitBearing INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE plants ADD COLUMN icon TEXT DEFAULT NULL")
        db.execSQL("UPDATE plants SET category = 'TREE' WHERE category NOT IN ('TREE', 'VEGETABLE')")
    }
}

@Database(
    entities = [PlantEntity::class, WateringLogEntity::class],
    version = 3,
    exportSchema = true
)
@androidx.room.TypeConverters(ShjiratiConverters::class)
abstract class ShjiratiDatabase : RoomDatabase() {
    abstract fun dao(): ShjiratiDao
}
