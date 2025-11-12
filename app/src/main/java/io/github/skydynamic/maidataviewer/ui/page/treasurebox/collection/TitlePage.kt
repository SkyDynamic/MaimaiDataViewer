package io.github.skydynamic.maidataviewer.ui.page.treasurebox.collection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource.LoadResult.Page.Companion.COUNT_UNDEFINED
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.data.MaimaiTitleData
import io.github.skydynamic.maidataviewer.core.manager.collection.CollectionType
import io.github.skydynamic.maidataviewer.core.noRippleClickable
import io.github.skydynamic.maidataviewer.core.paging.CollectionPagingSource
import io.github.skydynamic.maidataviewer.core.paging.PagingSourceState
import io.github.skydynamic.maidataviewer.core.strings
import io.github.skydynamic.maidataviewer.ui.component.UnknownProgressCircularProgress
import io.github.skydynamic.maidataviewer.ui.component.card.CollapsibleSearchCard
import io.github.skydynamic.maidataviewer.ui.component.card.PaginationCard
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

object TitlePageViewModel : ViewModel() {
    var isLoaded by mutableStateOf(false)
    var searchText by mutableStateOf("")
    var filterRate by mutableIntStateOf(-1)
    var isSearchCardCollapsed by mutableStateOf(false)
    var isSearching by mutableStateOf(false)
    var isSearchingActive by mutableStateOf(false)
    var searchJob by mutableStateOf<Job?>(null)
    var searchResult by mutableStateOf<Flow<PagingData<MaimaiTitleData>>?>(null)
    var searchResultState by mutableStateOf(
        PagingSourceState(0,0,0))
    var currentPage by mutableIntStateOf(0)
    var listState by mutableStateOf<LazyListState?>(null)

    var pickItem by mutableStateOf<MaimaiTitleData?>(null)

    fun search(
        keyword: String = searchText,
        rareType: Int = filterRate,
    ) = Pager(
        PagingConfig(
            pageSize = 10,
            enablePlaceholders = true,
            initialLoadSize = 30,
            prefetchDistance = 0,
            jumpThreshold = COUNT_UNDEFINED,
        )
    ) {
        val filterAction = if (rareType != -1) { list: List<MaimaiTitleData> ->
            list.filter {
                it.rareType.ordinal == rareType
            }
        } else null

        CollectionPagingSource.create<MaimaiTitleData>()
            .setManager(CollectionType.TITLE.getTypedManager()!!)
            .setKeyWord(keyword)
            .setFilterAction(filterAction)
            .setOnSearchFinished {
                searchResultState = it
            }.setCurrentPage(currentPage)
    }.also { searchResult = it.flow.cachedIn(viewModelScope) }
}

@Composable
fun TitlePage(
    onPicked: Boolean = false,
    onBackPressed: () -> Unit
) {
    val titleData = TitlePageViewModel.searchResult?.collectAsLazyPagingItems()

    fun search() {
        if (TitlePageViewModel.isSearchingActive && TitlePageViewModel.searchJob != null) {
            TitlePageViewModel.searchJob?.cancel()
        }

        TitlePageViewModel.searchJob = TitlePageViewModel.viewModelScope.launch(Dispatchers.IO) {
            TitlePageViewModel.isSearching = true
            TitlePageViewModel.isSearchingActive = true

            TitlePageViewModel.currentPage = 0

            TitlePageViewModel.search()

            TitlePageViewModel.searchJob = null
        }
    }

    if (TitlePageViewModel.listState == null) {
        TitlePageViewModel.listState = rememberLazyListState()
    }

    LaunchedEffect(TitlePageViewModel.currentPage) {
        val page = TitlePageViewModel.currentPage
        val total = TitlePageViewModel.searchResultState.totalPage
        if (page >= 0 && page < total) {
            TitlePageViewModel.listState?.scrollToItem(0)
            TitlePageViewModel.search()
        }
    }

    LaunchedEffect(Unit) {
        if (!CollectionType.TITLE.manager!!.isLoaded) {
            TitlePageViewModel.viewModelScope.launch(Dispatchers.IO) {
                CollectionType.TITLE.manager!!.loadCollectionData()
                TitlePageViewModel.isLoaded = true
            }
        } else {
            TitlePageViewModel.isLoaded = true
        }
        search()
    }

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = Icons.Default.Close.name,
                        modifier = Modifier.height(24.dp)
                    )
                }

                Text(
                    text = R.string.title_page.strings,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!TitlePageViewModel.isLoaded) {
                UnknownProgressCircularProgress(
                    strokeWidth = 4.dp,
                    gapSize = 4.dp
                )
            }

            AnimatedVisibility(
                visible = TitlePageViewModel.isLoaded,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500)),
                label = "TitlePage"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CollapsibleSearchCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        isCollapsed = TitlePageViewModel.isSearchCardCollapsed,
                        onCollapseToggle = {
                            TitlePageViewModel.isSearchCardCollapsed = !TitlePageViewModel.isSearchCardCollapsed
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = TitlePageViewModel.searchText,
                                onValueChange = { TitlePageViewModel.searchText = it },
                                enabled = TitlePageViewModel.isLoaded,
                                label = { Text(R.string.search.strings) },
                                placeholder = {
                                    Text(
                                        R.string.search_collection_placeholder.strings,
                                        autoSize = TextAutoSize.StepBased(minFontSize = 8.sp)
                                    )
                                },
                                leadingIcon = { Icon(Icons.Filled.Search, "") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = { search() }
                                ),
                                shape = RoundedCornerShape(25.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .height(65.dp)
                                    .weight(0.75f)
                            )

                            Button(
                                onClick = { search() },
                                enabled = TitlePageViewModel.isLoaded,
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp)
                                    .padding(4.dp)
                                    .weight(0.25f)
                            ) {
                                Text(
                                    text = R.string.search.strings,
                                    maxLines = 1,
                                    autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .heightIn(1000.dp)
                                .padding(top = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            state = TitlePageViewModel.listState!!
                        ) {
                            if (TitlePageViewModel.isSearchingActive) {
                                item(
                                    key = "search_result_title"
                                ) {
                                    ShadowElevatedCard(
                                        modifier = Modifier
                                            .heightIn(max = 40.dp)
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = R.string.title_search_result.strings
                                                    .format(
                                                        TitlePageViewModel.searchResultState.currentSearchCount
                                                    ),
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            )
                                        }
                                    }

                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                    )
                                }
                            }

                            if (titleData != null) {
                                items(
                                    count = titleData.itemCount,
                                    key = { titleData[it]?.id ?: it }
                                ) {
                                    val title = titleData[it]
                                    if (title != null) {
                                        HorizontalDivider(
                                            thickness = 2.dp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .noRippleClickable {
                                                    if (onPicked) {
                                                        TitlePageViewModel.pickItem = title
                                                        onBackPressed()
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painterResource(title.rareType.resId),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .size(36.dp)
                                            )

                                            Text(
                                                text = title.name ?: "",
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 54.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = Color.Black
                                            )
                                        }

                                        Text(
                                            text = title.normalText ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }

                            item(
                                key = "spacerBottom"
                            ) {
                                Spacer(modifier = Modifier.height(65.dp))
                            }
                        }

                        PaginationCard(
                            modifier = Modifier
                                .height(70.dp)
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .align(Alignment.BottomCenter),
                            currentPage = TitlePageViewModel.currentPage + 1,
                            totalPage = TitlePageViewModel.searchResultState.totalPage,
                        ) {
                            TitlePageViewModel.currentPage = it - 1
                        }
                    }
                }
            }
        }
    }
}