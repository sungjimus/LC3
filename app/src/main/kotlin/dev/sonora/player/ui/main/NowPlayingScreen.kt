
package dev.sonora.player.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import dev.sonora.player.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay


@Composable 
fun NowPlayingScreen(){

    var isPlaying by remember {mutableStateOf(true)}
    var progress by remember {mutableStateOf(0f)}
    var isDrag by remember {mutableStateOf(false)}
    var songCurrentIndex by remember {mutableStateOf(0)}

    LaunchedEffect(isPlaying,isDrag){

        while(isPlaying && !isDrag){

            delay(1000)
            progress=(progress+ 0.1f).coerceAtMost(1f)

            if(progress>=1f){

                progress=0f
            }
        }
        
    }


    Surface(
        modifier=Modifier.fillMaxSize()
    ){
        Column(
            modifier=Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp,Alignment.CenterVertically)

        ){
            AlbumArtwork()

            Text(
                text="Song title",
                fontSize= 22.sp

            )

            Text(
                text="Artist name",
                fontSize= 14.sp
            
            )

            ProgressBar(

                value= progress,
                progressChange= {newValue -> progress= newValue},
                dragChange= {isDrag=it}
            )


            PlayBackButton(
                isPlaying=isPlaying,
                onClick={
                    isPlaying=!isPlaying
                }
            )
        }

    }
}

@Composable
fun AlbumArtwork(){

    Image(
        painter=painterResource(id = R.drawable.album_art),
        contentDescription="Album Artwork",
        modifier=Modifier
            .size(250.dp)
            .clip(CircleShape)
    )
}

@Composable
fun PlayButton(isPlaying:Boolean,onClick: ()->Unit){

    
    IconButton(
        onClick=onClick
    ){

        if(isPlaying){
            Icon(
                imageVector= Icons.Default.Pause,
                contentDescription= "Pause"
            )
        }
        else{
            Icon(
                imageVector= Icons.Default.PlayArrow,
                contentDescription= "Play"
            )
        }
    }

}

@Composable
fun ProgressBar(value:Float,
    progressChange:(Float)->Unit,
    dragChange:(Boolean)->Unit){

        Slider(
            value=value,
            onValueChange= { newValue->
                progressChange(newValue)
                dragChange(true)
            },
            onValueChangeFinished={
                dragChange(false)
            }
        )   

}

@Composable
fun PlayBackButton(isPlaying:Boolean,onClick: ()->Unit){

    Row(
        horizontalArrangement=Arrangement.spacedBy(20.dp),
        verticalAlignment=Alignment.CenterVertically
    ){

        PlayButton(
            isPlaying=isPlaying,
            onClick=onClick

        )
    }
}
