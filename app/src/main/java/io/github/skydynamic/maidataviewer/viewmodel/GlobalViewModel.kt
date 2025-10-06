package io.github.skydynamic.maidataviewer.viewmodel

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

object GlobalViewModel : ViewModel() {
    var windowInsetsPadding by mutableStateOf(PaddingValues(0.dp))
}