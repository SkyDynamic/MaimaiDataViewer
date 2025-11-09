package io.github.skydynamic.maidataviewer.core.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.core.manager.MusicDataManager

class MusicDataPagingSource(
    var keyword: String = "",
    var genreId: Int? = null,
    var versionId: Int? = null,
    var currentPage: Int = 0
) : PagingSource<Int, MaimaiMusicData>() {
    private var onSearchFinish: (PagingSourceState) -> Unit = { 0 }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MaimaiMusicData> {
        return try {
            val key = params.key ?: 0
            val pageSize = params.loadSize

            val allData = MusicDataManager.instance.searchMusicData(keyword, genreId, versionId)
                .also {
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
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MaimaiMusicData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    fun setKeyWord(keyword: String): MusicDataPagingSource {
        this.keyword = keyword
        return this
    }

    fun setGenreId(genreId: Int?): MusicDataPagingSource {
        this.genreId = genreId
        return this
    }

    fun setVersionId(versionId: Int?): MusicDataPagingSource {
        this.versionId = versionId
        return this
    }

    fun setOnSearchFinished(onFinished: (PagingSourceState) -> Unit): MusicDataPagingSource {
        onSearchFinish = onFinished
        return this
    }

    fun setCurrentPage(currentPage: Int): MusicDataPagingSource {
        this.currentPage = currentPage
        return this
    }

    companion object {
        private var _instance: MusicDataPagingSource? = null

        val instance: MusicDataPagingSource
            get() {
                return _instance ?: MusicDataPagingSource().also {
                    _instance = it
                }
            }
    }
}
