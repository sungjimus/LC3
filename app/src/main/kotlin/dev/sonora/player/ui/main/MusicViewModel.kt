package dev.sonora.player.ui.main

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf

class MusicViewModel : ViewModel() {

    val songs= mutableStateListOf<Song>()
}