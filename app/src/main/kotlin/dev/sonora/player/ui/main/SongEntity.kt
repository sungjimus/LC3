package dev.sonora.player.ui.main

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="songs")
data class SongEntity(

    @PrimaryKey
    val uri: String,

    val id: Long,
    val artist: String,
    val title: String,
    val album: String,
    val duration: Long
)