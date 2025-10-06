package io.github.skydynamic.maidataviewer.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

object HomePageViewModel : ViewModel() {
    val isMetaLoading = mutableStateOf(true)
    val isUpdateMaiDataAvailable = mutableStateOf(false)
}