package io.github.skydynamic.maidataviewer.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource.LoadResult.Page.Companion.COUNT_UNDEFINED
import androidx.paging.cachedIn
import io.github.skydynamic.maidataviewer.core.data.Difficulty
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.core.paging.MusicDataPagingSource
import io.github.skydynamic.maidataviewer.core.paging.PagingSourceState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

object MusicPageViewModel : ViewModel() {
    val isLoadingMusic = mutableStateOf(true)
    val showLoadedFinishedMessage = mutableStateOf(false)

    val shouldScrollToTop = mutableStateOf(true)

    val searchText = mutableStateOf("")
    val genreFilter = mutableIntStateOf(-1)
    val versionFilter = mutableIntStateOf(-1)
    val levelFilterRange = mutableStateOf(1f..15f)
    val levelDifficultyTarget = Difficulty.entries.toMutableStateList()

    val isSearchCardCollapsed = mutableStateOf(false)
    val isSearching = mutableStateOf(false)
    val isSearchingActive = mutableStateOf(false)
    val searchJob = mutableStateOf<Job?>(null)
    val listState = mutableStateOf<LazyListState?>(null)
    val searchResult = mutableStateOf<Flow<PagingData<MaimaiMusicData>>?>(null)
    val searchResultState = mutableStateOf(
        PagingSourceState(0,0,0))
    val currentPage = mutableIntStateOf(0)

    fun search(
        keyword: String = searchText.value,
        genreId: Int? = if (genreFilter.intValue == -1) null else genreFilter.intValue,
        versionId: Int? = if (versionFilter.intValue == -1) null else versionFilter.intValue,
        levelRange: ClosedFloatingPointRange<Float> = levelFilterRange.value,
        targetDifficulty: List<Difficulty> = levelDifficultyTarget
    ) = Pager(
        PagingConfig(
            pageSize = 10,
            enablePlaceholders = true,
            initialLoadSize = 10,
            prefetchDistance = 0,
            jumpThreshold = COUNT_UNDEFINED,
        )
    ) {
        MusicDataPagingSource()
            .setKeyWord(keyword)
            .setGenreId(genreId)
            .setVersionId(versionId)
            .setLevelRange(levelRange)
            .setTargetDifficulty(targetDifficulty)
            .setOnSearchFinished {
                searchResultState.value = it
            }
            .setCurrentPage(currentPage.intValue)
    }.also { searchResult.value = it.flow.cachedIn(viewModelScope) }
}