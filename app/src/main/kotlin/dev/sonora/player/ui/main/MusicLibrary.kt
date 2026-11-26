package dev.sonora.player.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel


data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long
)


@Composable
fun MusicLibraryScreen(){

    val musicViewModel: MusicViewModel = viewModel()

    val context= LocalContext.current

    val permission= if(Build.VERSION.SDK_INT>= 33){
        Manifest.permission.READ_MEDIA_AUDIO
    }else{
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val hasPermission= ContextCompat.checkSelfPermission(
        context,
        permission
    )==PackageManager.PERMISSION_GRANTED

    val permissionLauncher= rememberLauncherForActivityResult(
        contract= ActivityResultContracts.RequestPermission()
    ){
        isGranted->
        if(isGranted){
            ScanMusic(context, musicViewModel)
        }else{
            Toast.makeText(
                context,
                "Permission required to scan music",
                Toast.LENGTH_SHORT
            ).show()

        }
    }

    Column {

        Button(
            onClick={
                if(hasPermission){
                    ScanMusic(context, musicViewModel)
                }
                else{
                    permissionLauncher.launch(permission)
                }
            }
        ){
            Text("Scan For Music")
        }

        Text(
            text = "Library",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        SongList(musicViewModel.songs)
    }
    
}

fun ScanMusic(context: Context, musicViewModel: MusicViewModel){

    val collection= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    
    val projection= arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION
    )
    
    val cursor= context.contentResolver.query(
        collection,
        projection,
        null,
        null,
        null
    )

    cursor?.use { cursor ->

        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

        musicViewModel.songs.clear()

        while(cursor.moveToNext()){
            val id= cursor.getLong(idColumn)
            val title= cursor.getString(titleColumn)
            val artist= cursor.getString(artistColumn)
            val album= cursor.getString(albumColumn)
            val duration= cursor.getLong(durationColumn)

            val song= Song(id,title,artist,album,duration)

            musicViewModel.songs.add(song)
        }
    }

}

@Composable
fun SongList(songs: List<Song>){

    LazyColumn{

        items(songs){ song->
            Column{

                Row(
                    modifier=Modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.SpaceBetween
                ){
                    Text(text=song.title)
                    Text(text=formatDuration(song.duration))
                }

                Text(text=song.artist)

            }

        }
    }
}

fun formatDuration(duration: Long): String {

    val minutes = duration / 60000
    val seconds = (duration % 60000) / 1000

    return "%d:%02d".format(minutes, seconds)
}
