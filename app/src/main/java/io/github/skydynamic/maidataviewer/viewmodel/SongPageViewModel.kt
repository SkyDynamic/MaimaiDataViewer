package io.github.skydynamic.maidataviewer.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import kotlinx.coroutines.Job

object SongPageViewModel : ViewModel() {
    val isLoadingMusic = mutableStateOf(true)
    val showLoadedFinishedMessage = mutableStateOf(false)

    val shouldScrollToTop = mutableStateOf(true)

    val searchText = mutableStateOf("")
    val genreFilter = mutableIntStateOf(-1)
    val versionFilter = mutableIntStateOf(-1)

    val isSearching = mutableStateOf(false)
    val isSearchingActive = mutableStateOf(false)
    val searchJob = mutableStateOf<Job?>(null)

    val searchResult = mutableStateOf(emptyList<MaimaiMusicData>())

    val listState = mutableStateOf<LazyListState?>(null)
}