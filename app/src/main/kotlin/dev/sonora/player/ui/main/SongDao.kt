
package dev.sonora.player.ui.main

import androidx.room.Dao
import androidx.room.Upsert
import androidx.room.Delete
import androidx.room.Query

@Dao
interface SongDao {

    @Upsert
    suspend fun insertSong(song: SongEntity)

    @Delete
    suspend fun deleteSong(song: SongEntity)

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<SongEntity>
}
