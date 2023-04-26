package com.desertcamels.camel.screens

import android.widget.ProgressBar
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import com.desertcamels.camel.models.MainViewModel
import com.desertcamels.camel.ui.theme.CamelTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val viewModel: MainViewModel = MainViewModel()

@Composable
fun MainScreen() {
    val downloadStatus by viewModel.downloadState.collectAsState("Downloading...")

    Box(modifier = Modifier.padding(16.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DownloadStatus(status = downloadStatus)
            Spacer(modifier = Modifier.height(16.dp))
            ProgressBar()
        }

    }
}


@Composable
fun DownloadStatus(status: String) {
    Text(text = status)
}

@Composable
fun ProgressBar() {
    val progress by viewModel.progressState.collectAsState(0f)

    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CamelTheme {
        MainScreen()
    }
}