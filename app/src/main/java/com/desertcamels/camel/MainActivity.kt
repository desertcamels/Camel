package com.desertcamels.camel

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.desertcamels.camel.services.DownloadService
import com.desertcamels.camel.ui.theme.CamelTheme

class MainActivity : ComponentActivity() {
    private val regexUrls =
        Regex("(?:(?:https?|ftp)://)?[\\w\\d\\-_]+(?:\\.[\\w\\d\\-_]+)+[\\w\\d\\-.,@?^=%&amp;:/~+#]*[\\w\\d@?^=%&amp;/~+#]")
    private val downloadStatus = MutableLiveData<String>()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

        val myApplication = application as App
        myApplication.downloadRepository.downloadStatus.observe(this) { status ->
            Log.d("MainActivity", status)
        }

        setContent {
            CamelTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DownloadStatus(downloadStatus = downloadStatus)
                }
            }
        }
    }

    private fun onPermissionGranted() {
        // Permission is granted. Continue the action or workflow in your
        // app.
        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
    }

    private fun onPermissionDenied() {
        // Explain to the user that the feature is unavailable because
        // the features requires a permission that the user has denied.
        // At the same time, respect the user's decision. Don't link to
        // system settings in an effort to convince the user to change
        // their decision.
        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
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

            downloadStatus.postValue("Downloading: $sharedUrl")

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

}

@Composable
fun DownloadStatus(downloadStatus: LiveData<String>) {
    Text(text = downloadStatus.value ?: "Downloading...")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CamelTheme {
        DownloadStatus(downloadStatus = MutableLiveData("Downloading: https://www.youtube.com/watch?v=QH2-TGUlwu4"))
    }
}
