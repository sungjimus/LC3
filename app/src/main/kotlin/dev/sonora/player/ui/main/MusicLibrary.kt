import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import android.provider.MediaStore
import android.content.ContentUris

@Composable
fun MusicLibraryScreen(){

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
            ScanMusic()
        }else{

        }
    }

    Button(
        onClick={
            if(hasPermission){
                ScanMusic()
            }
            else{
                permissionLauncher.launch(permission)
            }
        }
    ){
        Text("Scan For Music")
    }

    
}

fun ScanMusic(){

    
}