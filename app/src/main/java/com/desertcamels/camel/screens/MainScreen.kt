package com.desertcamels.camel.screens

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import com.desertcamels.camel.models.MainViewModel
import com.desertcamels.camel.ui.theme.CamelTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import com.google.accompanist.permissions.*

private const val TAG = "MainScreen"
private val viewModel: MainViewModel = MainViewModel()

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val downloadStatus by viewModel.downloadState.collectAsState()
    val progress by viewModel.progressState.collectAsState(0f)
    val postNotificationPermissionState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS
    )
    val storagePermissionState = rememberPermissionState(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    Log.d(TAG, "MainScreen: $downloadStatus, $progress")

    Scaffold(
        topBar = { AppTopBar() },
        bottomBar = { AppBottomBar() },
        floatingActionButton = { AppFab() },
        floatingActionButtonPosition = FabPosition.End,
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if ((!postNotificationPermissionState.status.isGranted) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                FeatureThatRequiresPostNotificationPermission(postNotificationPermissionState)
            } else if (!storagePermissionState.status.isGranted) {
                FeatureThatRequiresStoragePermission(storagePermissionState)
            } else {
                DownloadStatus(status = downloadStatus)
            }
        }
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
                "The storage is important for this app. Please grant the permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "Storage permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow, textAlign = TextAlign.Center)
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