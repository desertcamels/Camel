package com.desertcamels.camel.screens

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

private const val TAG = "MainScreen"
private val viewModel: MainViewModel = MainViewModel()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val downloadStatus by viewModel.downloadState.collectAsState()
    val progress by viewModel.progressState.collectAsState(0f)
    Log.d(TAG, "MainScreen: $downloadStatus, $progress")

    Scaffold(
        topBar = { AppTopBar() },
        bottomBar = { AppBottomBar() },
        floatingActionButton = { AppFab() },
        floatingActionButtonPosition = FabPosition.End,
    ) {
        Column(
            modifier = Modifier.padding(it).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            DownloadStatus(status = downloadStatus)
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


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CamelTheme {
        MainScreen()
    }
}