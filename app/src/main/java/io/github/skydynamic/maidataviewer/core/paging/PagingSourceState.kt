package io.github.skydynamic.maidataviewer.core.paging

data class PagingSourceState(
    val currentPage: Int = 0,
    val totalPage: Int = 0,
    val currentSearchCount: Int = 0
)
