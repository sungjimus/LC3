package dev.sonora.player.ui.main

import android.content.Context
import androidx.room.Room

object DatabaseProvider{

    private var INSTANCE: MusicDatabase? = null

    @Synchronized
    fun getDatabase(context: Context): MusicDatabase {

        if (INSTANCE != null) {
            return INSTANCE!!
        }
        else {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                MusicDatabase::class.java,
                "lc3_database"
            ).build()

            return INSTANCE!!
    }
}
}