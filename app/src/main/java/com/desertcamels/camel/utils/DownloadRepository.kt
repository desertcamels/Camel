package com.desertcamels.camel.utils

import androidx.lifecycle.MutableLiveData

class DownloadRepository {
    val downloadStatus = MutableLiveData<String>()
}