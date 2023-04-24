package com.desertcamels.camel.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.desertcamels.camel.R
import com.desertcamels.camel.utils.DownloadRepository
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val NOTIFICATION_ID = 1
private const val CHANNEL_ID = "DownloadChannel"
private const val TAG = "DownloadService"
class DownloadService() : Service() {
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var downloadRepository: DownloadRepository

    override fun onCreate() {
        super.onCreate()
        downloadRepository = DownloadRepository()
        try {
            YoutubeDL.getInstance().init(this)
        } catch (e: YoutubeDLException) {
            Log.e(TAG, "failed to initialize youtubedl-android", e)
        }

        notificationManager = NotificationManagerCompat.from(this)

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Download in progress")
        startForeground(NOTIFICATION_ID, notification)
        if (intent != null) {
            intent.getStringExtra("URL")?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    startDownload(it)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotification(contentText: String): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading File")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.baseline_download_24)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Download Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        return builder.build()
    }

    private suspend fun startDownload(url: String) = withContext(Dispatchers.IO) {
        val youtubeDLDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Camel"
        )
        val request = YoutubeDLRequest(url)
        request.addOption("-o", youtubeDLDir.absolutePath + "/%(title)s.%(ext)s")
        YoutubeDL.getInstance().execute(
            request
        ) { _: Float, _: Long, line: String ->
            Log.d(TAG, line)
            downloadRepository.downloadStatus.postValue(line)
            notify(line)

        }
    }

    private fun notify(status: String) {

        val notification = createNotification(status)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}