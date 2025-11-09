package io.github.skydynamic.maidataviewer.core.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.skydynamic.maidataviewer.core.data.IMaimaiCollectionData
import io.github.skydynamic.maidataviewer.core.manager.collection.CollectionManager

class CollectionPagingSource<T : IMaimaiCollectionData, M : CollectionManager<T>>(
    var keyword: String = "",
    var currentPage: Int = 0
) : PagingSource<Int, T>() {
    private var filterAction: ((List<T>) -> List<T>)? = null
    private var onSearchFinish: (PagingSourceState) -> Unit = { 0 }
    private var manager: M? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            val manager = manager
            if (manager != null) {
                val key = params.key ?: 0
                val pageSize = params.loadSize

                val allData = manager.search(keyword, filterAction).also {
                    val totalPage = if (it.isNotEmpty()) (it.size + pageSize - 1) / pageSize else 0
                    onSearchFinish(PagingSourceState(
                        currentPage,
                        totalPage,
                        it.size
                    ))
                }

                val sublist = allData.drop(currentPage * pageSize).take(pageSize)

                LoadResult.Page(
                    data = sublist,
                    prevKey = null,
                    nextKey = null
                )
            } else {
                LoadResult.Error(Exception("CollectionManager is not set"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    fun setKeyWord(keyword: String): CollectionPagingSource<T, M> {
        this.keyword = keyword
        return this
    }

    fun setFilterAction(filterAction: ((List<T>) -> List<T>)?): CollectionPagingSource<T, M> {
        this.filterAction = filterAction
        return this
    }

    fun setCurrentPage(currentPage: Int): CollectionPagingSource<T, M> {
        this.currentPage = currentPage
        return this
    }

    fun setOnSearchFinished(onFinished: (PagingSourceState) -> Unit): CollectionPagingSource<T, M> {
        onSearchFinish = onFinished
        return this
    }

    fun setManager(manager: M): CollectionPagingSource<T, M> {
        this.manager = manager
        return this
    }

    companion object {
        inline fun <reified T : IMaimaiCollectionData> create(): CollectionPagingSource<T, CollectionManager<T>> {
            return CollectionPagingSource()
        }
    }
}