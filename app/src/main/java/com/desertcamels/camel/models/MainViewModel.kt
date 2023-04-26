package com.desertcamels.camel.models

import androidx.lifecycle.ViewModel
import com.desertcamels.camel.MainActivityState

class MainViewModel : ViewModel() {
    private val _downloadState = MainActivityState.downloadState
    private val _progressState = MainActivityState.progressState
    val downloadState = _downloadState
    val progressState = _progressState
}