package io.github.skydynamic.maidataviewer.ui.page.treasurebox.collection

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource.LoadResult.Page.Companion.COUNT_UNDEFINED
import androidx.paging.cachedIn
import io.github.skydynamic.maidataviewer.core.data.MaimaiCommonCollectionData
import io.github.skydynamic.maidataviewer.core.manager.collection.CollectionType
import io.github.skydynamic.maidataviewer.core.paging.CollectionPagingSource
import io.github.skydynamic.maidataviewer.core.paging.PagingSourceState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

abstract class BaseCollectionViewModel : ViewModel() {
    var isLoaded by mutableStateOf(false)
    var searchText by mutableStateOf("")
    var filterGenre by mutableIntStateOf(-1)
    var isSearchCardCollapsed by mutableStateOf(false)
    var isSearching by mutableStateOf(false)
    var isSearchingActive by mutableStateOf(false)
    var searchJob by mutableStateOf<Job?>(null)
    var searchResult by mutableStateOf<Flow<PagingData<MaimaiCommonCollectionData>>?>(null)
    var searchResultState by mutableStateOf(PagingSourceState(0, 0, 0))
    var currentPage by mutableIntStateOf(0)
    var listState by mutableStateOf<LazyGridState?>(null)

    abstract fun getCollectionType(): CollectionType

    fun search(
        keyword: String = searchText,
        genre: Int = filterGenre,
    ) = Pager(
        PagingConfig(
            pageSize = 10,
            enablePlaceholders = true,
            initialLoadSize = 10,
            prefetchDistance = 0,
            jumpThreshold = COUNT_UNDEFINED,
        )
    ) {
        val filterAction = if (filterGenre != -1) { list: List<MaimaiCommonCollectionData> ->
            list.filter { it.genre == genre }
        } else null

        CollectionPagingSource.create<MaimaiCommonCollectionData>()
            .setManager(getCollectionType().getTypedManager()!!)
            .setKeyWord(keyword)
            .setFilterAction(filterAction)
            .setOnSearchFinished { searchResultState = it }
            .setCurrentPage(currentPage)
    }.also { searchResult = it.flow.cachedIn(viewModelScope) }
}
