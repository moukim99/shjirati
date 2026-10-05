package com.moukim.shjirati.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var instance: ShjiratiDatabase? = null

    fun get(context: Context): ShjiratiDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                ShjiratiDatabase::class.java,
                "shjirati.db"
            ).build().also { instance = it }
        }
}
