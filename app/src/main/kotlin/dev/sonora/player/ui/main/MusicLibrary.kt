package dev.sonora.player.ui.main

import android.content.Context
import android.net.Uri
import android.media.MediaMetadataRetriever
import android.provider.DocumentsContract
import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.documentfile.provider.DocumentFile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


data class Song(
    val id: Long,
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long
)

data class MusicFolder(
    val uri: String,
    val name: String
)


@Composable
fun MusicLibraryScreen() {

    val context = LocalContext.current

    val database = DatabaseProvider.getDatabase(context)
    val factory = MusicViewModelFactory(database.songDao())
    val musicViewModel: MusicViewModel = viewModel(factory = factory)

    val preferences = remember {
        context.getSharedPreferences("lc3_folders", Context.MODE_PRIVATE)
    }

    val folders = remember {
        mutableStateListOf<MusicFolder>()
    }

    var selectedFolder by remember {
        mutableStateOf<MusicFolder?>(null)
    }

    var folderSongs by remember {
        mutableStateOf<List<Song>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    // Load previously selected folders
    LaunchedEffect(Unit) {

        val savedFolders = preferences.getStringSet("folders", emptySet())
            ?: emptySet()

        savedFolders.forEach { folderUri ->

            val uri = Uri.parse(folderUri)

            val folder = DocumentFile.fromTreeUri(context, uri)

            if (folder != null && folder.canRead()) {
                folders.add(
                    MusicFolder(
                        uri = folderUri,
                        name = folder.name ?: "Unknown folder"
                    )
                )
            }
        }
    }

    // Native Android folder picker
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->

        if (uri != null) {

            try {

                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val folder = DocumentFile.fromTreeUri(context, uri)

                if (folder != null) {

                    val alreadyAdded = folders.any {
                        it.uri == uri.toString()
                    }

                    if (!alreadyAdded) {

                        val newFolder = MusicFolder(
                            uri = uri.toString(),
                            name = folder.name ?: "Unknown folder"
                        )

                        folders.add(newFolder)

                        preferences.edit()
                            .putStringSet(
                                "folders",
                                folders.map { it.uri }.toSet()
                            )
                            .apply()
                    }
                }

            } catch (e: SecurityException) {

                Toast.makeText(
                    context,
                    "Could not access this folder",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Load songs whenever a folder is opened
    LaunchedEffect(selectedFolder?.uri) {

        val folder = selectedFolder

        if (folder != null) {

            isLoading = true

            val songs = withContext(Dispatchers.IO) {
                scanFolder(
                    context,
                    Uri.parse(folder.uri)
                )
            }

            folderSongs = songs

            songs.forEach { song ->
                musicViewModel.saveSong(song)
            }

            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (selectedFolder == null) {

                Text(
                    text = "Library",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        folderPicker.launch(null)
                    }
                ) {
                    Text("Add Folder")
                }

            } else {

                Column {

                    TextButton(
                        onClick = {
                            selectedFolder = null
                            folderSongs = emptyList()
                        }
                    ) {
                        Text("← Folders")
                    }

                    Text(
                        text = selectedFolder!!.name,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider()

        if (selectedFolder == null) {

            if (folders.isEmpty()) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 70.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "No folders added yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Add a folder to start building your library",
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {

                    items(folders, key = { it.uri }) { folder ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFolder = folder
                                }
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "📁",
                                fontSize = 25.sp
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 14.dp)
                            ) {

                                Text(
                                    text = folder.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = "Open folder",
                                    fontSize = 12.sp
                                )
                            }

                            TextButton(
                                onClick = {

                                    folders.remove(folder)

                                    preferences.edit()
                                        .putStringSet(
                                            "folders",
                                            folders.map { it.uri }.toSet()
                                        )
                                        .apply()
                                }
                            ) {
                                Text("Remove")
                            }
                        }

                        HorizontalDivider()
                    }
                }
            }

        } else {

            if (isLoading) {

                Text(
                    text = "Loading songs...",
                    modifier = Modifier.padding(top = 20.dp)
                )

            } else if (folderSongs.isEmpty()) {

                Text(
                    text = "No audio files found in this folder",
                    modifier = Modifier.padding(top = 20.dp)
                )

            } else {

                FolderSongList(folderSongs)
            }
        }
    }
}


@Composable
fun FolderSongList(songs: List<Song>) {

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        items(songs, key = { it.uri }) { song ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = song.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatDuration(song.duration),
                    fontSize = 13.sp,
                    modifier = Modifier.width(48.dp),
                    textAlign = TextAlign.End
                )
            }

            HorizontalDivider()
        }
    }
}


// Scan a selected folder and its subfolders
fun scanFolder(
    context: Context,
    folderUri: Uri
): List<Song> {

    val rootFolder = DocumentFile.fromTreeUri(context, folderUri)
        ?: return emptyList()

    val songs = mutableListOf<Song>()

    fun scanDirectory(directory: DocumentFile) {

        val files = directory.listFiles()

        for (file in files) {

            if (file.isDirectory) {

                scanDirectory(file)

            } else {

                val extension = file.name
                    ?.substringAfterLast(".", "")
                    ?.lowercase()

                val audioExtensions = setOf(
                    "mp3", "m4a", "aac", "flac",
                    "wav", "ogg", "opus", "wma"
                )

                val isAudio = file.type?.startsWith("audio/") == true ||
                    extension in audioExtensions

                if (!isAudio) continue

                val retriever = MediaMetadataRetriever()

                try {

                    retriever.setDataSource(context, file.uri)

                    val title = retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_TITLE
                    ) ?: file.name?.substringBeforeLast(".")
                    ?: "Unknown title"

                    val artist = retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_ARTIST
                    ) ?: ""

                    val album = retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_ALBUM
                    ) ?: ""

                    val duration = retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_DURATION
                    )?.toLongOrNull() ?: 0L

                    songs.add(
                        Song(
                            id = file.uri.toString().hashCode().toLong(),
                            uri = file.uri.toString(),
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration
                        )
                    )

                } catch (e: Exception) {

                } finally {

                    try {
                        retriever.release()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    try {
        scanDirectory(rootFolder)
    } catch (_: Exception) {
        
    }

    return songs.sortedBy { it.title.lowercase() }
}


fun formatDuration(duration: Long): String {

    val minutes = duration / 60000
    val seconds = (duration % 60000) / 1000

    return "%d:%02d".format(minutes, seconds)
}
