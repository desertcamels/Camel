package com.desertcamels.camel

import android.app.Application
import com.desertcamels.camel.utils.DownloadRepository

class App: Application() {
    val downloadRepository = DownloadRepository()
}