package com.desertcamels.camel.screens

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

@Composable
fun MainScreen() {
    val downloadStatus by viewModel.downloadState.collectAsState()
    val progress by viewModel.progressState.collectAsState(0f)
    Log.d(TAG, "MainScreen: $downloadStatus, $progress")

    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                AppTitle()
            }
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DownloadStatus(status = downloadStatus)
            }
            Column() {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp), horizontalArrangement = Arrangement.End
                ) {
                    AppFab()
                }
                AppNavigationBar()
            }

        }
    }
}

@Composable
fun DownloadStatus(status: String) {
    Text(text = status, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
}

@Composable
fun AppNavigationBar() {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            onClick = { /*TODO*/ },
            selected = true
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Downloads") },
            label = { Text("Downloads") },
            onClick = { /*TODO*/ },
            selected = false
        )

    }
}

@Composable
fun AppTitle() {
    Text(
        text = "Camel",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Black
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