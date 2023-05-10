package com.desertcamels.camel

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.desertcamels.camel.services.DownloadService
import com.desertcamels.camel.ui.theme.CamelTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.flow.MutableStateFlow

private const val TAG = "MainActivity"

data class VideoInfo(val title: String?, val thumbnail: String?, val url: String?)

object DownloadState {
    val downloadState = MutableStateFlow("Please share a 🔗 to download")
    val progressState: MutableStateFlow<Float> = MutableStateFlow(0f)
    val videoState: MutableStateFlow<VideoInfo?> = MutableStateFlow(null)
}

class MainActivity : ComponentActivity() {
    private val regexUrls =
        Regex("(?:(?:https?|ftp)://)?[\\w\\d\\-_]+(?:\\.[\\w\\d\\-_]+)+[\\w\\d\\-.,@?^=%&amp;:/~+#]*[\\w\\d@?^=%&amp;/~+#]")

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CamelTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ModalNavigationDrawer(drawerContent = { AppDrawer() }) {
                        MainScreen()
                    }
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            val action = it.action
            val type = it.type

            if (Intent.ACTION_SEND == action && type != null && "text/plain" == type) {
                handleReceivedText(it)
            }

        }
    }

    private fun handleReceivedText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            val urls = regexUrls.findAll(sharedText).map { it.value }.toList()
            var sharedUrl = urls.first()

            println(sharedUrl)

            if (sharedUrl.contains("youtu.be"))
                sharedUrl = convertToFullUrl((sharedUrl))

            println(sharedUrl)
            val downloadIntent = Intent(this, DownloadService::class.java)
            downloadIntent.putExtra("URL", sharedUrl)
            startService(downloadIntent)
        }
    }

    private fun convertToFullUrl(shortUrl: String): String {
        val baseUrl = "https://www.youtube.com/watch?v="
        val videoId = shortUrl.substringAfterLast("/")
        return baseUrl + videoId
    }

    override fun onDestroy() {
        super.onDestroy()
        //stop foreground service
        val stopIntent = Intent(this, DownloadService::class.java)
        stopService(stopIntent)
    }

}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val downloadStatus by DownloadState.downloadState.collectAsState()
    val videoState by DownloadState.videoState.collectAsState()
    val progress by DownloadState.progressState.collectAsState(0f)
    Log.d(TAG, "MainScreen: $downloadStatus, $progress")
    val postNotificationPermissionState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS
    )
    val storagePermissionState = rememberPermissionState(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    Scaffold(
        topBar = { AppTopBar() },
        bottomBar = { AppBottomBar() },
        floatingActionButton = { AppFab() },
        floatingActionButtonPosition = FabPosition.End,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if ((!postNotificationPermissionState.status.isGranted) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                FeatureThatRequiresPostNotificationPermission(postNotificationPermissionState)
            } else if (!storagePermissionState.status.isGranted && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                FeatureThatRequiresStoragePermission(storagePermissionState)
            } else {
                Thumbnail(videoState = videoState)
                Spacer(modifier = Modifier.height(16.dp))
                DownloadStatus(status = downloadStatus)
            }
        }
    }
}

@Composable
fun AppDrawer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // create vector image header for the app drawer
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Account Circle",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        )
        Text(text = "This is the drawer")
    }
}

@Composable
fun Thumbnail(videoState: VideoInfo?) {
    if (videoState != null) {
        Image(
            painter = rememberImagePainter(data = videoState?.thumbnail, builder = {
                crossfade(true)
                placeholder(R.drawable.camel_thumbnail_placeholder)
                error(R.drawable.baseline_error_24)
            }),
            contentDescription = "Thumbnail of ${videoState?.title}",
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.camel_thumbnail_placeholder),
            contentDescription = "Placeholder until video is loaded",
            modifier = Modifier.size(200.dp)
        )
    }
}

@Composable
fun DownloadStatus(status: String) {
    Text(text = status, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
}

@Composable
fun AppBottomBar() {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            onClick = { /*TODO*/ },
            selected = true
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Done, contentDescription = "Downloads") },
            label = { Text("Downloads") },
            onClick = { /*TODO*/ },
            selected = false
        )

    }
}

@Composable
fun AppTopBar() {
    SmallTopAppBar(
        title = { Text("Camel") },
        navigationIcon = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        }
    )
}

@Composable
fun AppFab() {
    FloatingActionButton(
        onClick = { /*TODO*/ },
        content = { Icon(Icons.Filled.Add, contentDescription = "Add") }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun FeatureThatRequiresStoragePermission(permissionState: PermissionState) {

    if (permissionState.status.isGranted) {
        Text("Storage permission Granted")
    } else {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (permissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "The storage is important for this app. Please grant the permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "Storage permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun FeatureThatRequiresPostNotificationPermission(permissionState: PermissionState) {

    if (permissionState.status.isGranted) {
        Text("Storage permission Granted")
    } else {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (permissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "Notifications are important for this app. Please grant the permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "Notification permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CamelTheme {
        MainScreen()
    }
}
