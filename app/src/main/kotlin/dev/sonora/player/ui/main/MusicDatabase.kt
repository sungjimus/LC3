package dev.sonora.player.ui.main

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room

@Database(entities=[SongEntity::class],version=1)
abstract class MusicDatabase: RoomDatabase(){

    abstract fun songDao(): SongDao

}