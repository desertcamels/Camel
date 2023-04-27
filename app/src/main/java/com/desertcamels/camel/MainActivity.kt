package com.desertcamels.camel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.desertcamels.camel.screens.MainScreen
import com.desertcamels.camel.services.DownloadService
import com.desertcamels.camel.ui.theme.CamelTheme
import kotlinx.coroutines.flow.MutableStateFlow

object MainActivityState {
    val downloadState = MutableStateFlow("Please share a 🔗 to download")
    val progressState: MutableStateFlow<Float> = MutableStateFlow(0f)
}
class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_CODE = 1
    }
    private val regexUrls =
        Regex("(?:(?:https?|ftp)://)?[\\w\\d\\-_]+(?:\\.[\\w\\d\\-_]+)+[\\w\\d\\-.,@?^=%&amp;:/~+#]*[\\w\\d@?^=%&amp;/~+#]")

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
                    MainScreen()
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
