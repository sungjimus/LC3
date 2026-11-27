
package dev.sonora.player.ui.main

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MusicViewModel(

    private val songDao: SongDao
) : ViewModel() {

    val songs= mutableStateListOf<Song>()

    fun loadSongs() {
        viewModelScope.launch {
            val savedSongs = songDao.getAllSongs()

            songs.clear()

            savedSongs.forEach { entity ->
                songs.add(
                    Song(
                        id = entity.id,
                        uri = entity.uri,
                        title = entity.title,
                        artist = entity.artist,
                        album = entity.album,
                        duration = entity.duration
                    )
                )
            }
        }
    }

    fun saveSong(song: Song) {
        viewModelScope.launch {
            songDao.insertSong(
                SongEntity(
                    uri = song.uri,
                    id = song.id,
                    title = song.title,
                    artist = song.artist,
                    album = song.album,
                    duration = song.duration
                )
            )
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            songDao.deleteSong(
                SongEntity(
                    uri = song.uri,
                    id = song.id,
                    title = song.title,
                    artist = song.artist,
                    album = song.album,
                    duration = song.duration
                )
            )

            songs.remove(song)
        }
    }
}
