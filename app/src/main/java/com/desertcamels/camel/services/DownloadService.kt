package com.desertcamels.camel.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.desertcamels.camel.MainActivity
import com.desertcamels.camel.DownloadState
import com.desertcamels.camel.R
import com.desertcamels.camel.VideoInfo
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL.getInstance
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


private const val TAG = "DownloadService"

class DownloadService : Service() {
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var scope: CoroutineScope
    private lateinit var mainIntent: Intent
    private lateinit var mainPendingIntent: PendingIntent
    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)
        scope = CoroutineScope(Dispatchers.IO)
        mainIntent = Intent(this, MainActivity::class.java)
        mainPendingIntent = PendingIntent.getActivity(this, 1, mainIntent, FLAG_IMMUTABLE)
        try {
            getInstance().init(this)
            FFmpeg.getInstance().init(this);
            Aria2c.getInstance().init(this);
        } catch (e: YoutubeDLException) {
            Log.e(TAG, "failed to initialize youtubedl-android", e)
        }


        // Create the notification
        val notification = buildNotification("Downloading")

        // Start the service and show the notification
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("URL")?.let {
            CoroutineScope(Dispatchers.Main).launch {
                startDownload(it)
            }
        }

        return START_STICKY
    }

    private suspend fun startDownload(url: String) = withContext(Dispatchers.IO) {
        val youtubeDLDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Camel"
        )

        val streamInfo = getInstance().getInfo(url)
        val request = YoutubeDLRequest(url)
        // echo Download complete when the download is finished
        request.addOption("-o", youtubeDLDir.absolutePath + "/%(title)s.%(ext)s")
        request.addOption("--downloader", "libaria2c.so")
        request.addOption("--external-downloader-args", "aria2c:\"--summary-interval=1\"")

        getInstance().execute(
            request
        ) { progress: Float, _: Long, line: String ->
            notify(progress.toString())
            val videoInfo = VideoInfo(
                streamInfo.title,
                streamInfo.thumbnail,
                streamInfo.url
            )
            Log.d(TAG, videoInfo.toString())
            CoroutineScope(Dispatchers.Main).launch {
                updateStates(progress, line, videoInfo)
            }

        }
    }

    private fun buildNotification(status: String): Notification {
        val builder = NotificationCompat.Builder(this, "DownloadService")
            .setContentTitle("Downloading")
            .setContentText(status)
            .setSmallIcon(R.drawable.baseline_download_24)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(mainPendingIntent)
            .setOnlyAlertOnce(true)

        val channel = NotificationChannel(
            "DownloadService",
            "Download Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        return builder.build()
    }

    private fun notify(status: String) {
        val notification = buildNotification(status)

        notificationManager.notify(1, notification)
    }

    private suspend fun updateStates(progress: Float, line: String, video: VideoInfo) =
        withContext(Dispatchers.IO) {
            DownloadState.downloadState.emit(line)
            DownloadState.progressState.emit(progress)
            DownloadState.videoState.emit(video)

            Log.d(TAG, video.toString())

            if (line.contains("[download] 100%")) {
                DownloadState.downloadState.emit("Download complete.\nPlease share another 🔗 to download")
                //stopSelf()
            }
        }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the service and remove the notification
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}
