package com.moukim.shjirati.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: ShjiratiDatabase? = null

    fun get(context: Context): ShjiratiDatabase =
        INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                ShjiratiDatabase::class.java,
                "shjirati.db"
            )
                .build()
                .also { INSTANCE = it }
        }
}
