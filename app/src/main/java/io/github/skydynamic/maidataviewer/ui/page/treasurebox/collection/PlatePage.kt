package io.github.skydynamic.maidataviewer.ui.page.treasurebox.collection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.data.MaimaiPlateData
import io.github.skydynamic.maidataviewer.core.getString
import io.github.skydynamic.maidataviewer.core.manager.collection.CollectionType
import io.github.skydynamic.maidataviewer.core.manager.resource.ResourceManagerType
import io.github.skydynamic.maidataviewer.core.paging.CollectionPagingSource
import io.github.skydynamic.maidataviewer.core.paging.PagingSourceState
import io.github.skydynamic.maidataviewer.ui.component.UnknownProgressCircularProgress
import io.github.skydynamic.maidataviewer.ui.component.card.CollapsibleSearchCard
import io.github.skydynamic.maidataviewer.ui.component.card.PaginationCard
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File

object PlatePageViewModel : ViewModel() {
    var isLoaded by mutableStateOf(false)
    var searchText by mutableStateOf("")
    var filterGenre by mutableIntStateOf(-1)
    var isSearchCardCollapsed by mutableStateOf(false)
    var isSearching by mutableStateOf(false)
    var isSearchingActive by mutableStateOf(false)
    var searchJob by mutableStateOf<Job?>(null)
    var searchResult by mutableStateOf<Flow<PagingData<MaimaiPlateData>>?>(null)
    var searchResultState by mutableStateOf(
        PagingSourceState(0,0,0))
    var currentPage by mutableIntStateOf(0)
    var listState by mutableStateOf<LazyGridState?>(null)

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
        val filterAction = if (filterGenre != -1) { list: List<MaimaiPlateData> ->
            list.filter {
                it.genre == genre
            }
        } else null

        CollectionPagingSource.create<MaimaiPlateData>()
            .setManager(CollectionType.PLATE.getTypedManager()!!)
            .setKeyWord(keyword)
            .setFilterAction(filterAction)
            .setOnSearchFinished {
                searchResultState = it
            }.setCurrentPage(currentPage)
    }.also { searchResult = it.flow.cachedIn(viewModelScope) }
}

@Composable
fun PlateSimpleCard(
    plateData: MaimaiPlateData
) {
    val plateResManager = ResourceManagerType.PLATE.instance!!

    val defaultPlateByte = remember { plateResManager.getResByteFromAssets(0) }
    var plateFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(plateData.id) {
        PlatePageViewModel.viewModelScope.launch(Dispatchers.IO) {
            plateFile = try {
                ResourceManagerType.PLATE.instance!!.getResFile(plateData.id)
            } catch (_: Exception) {
                null
            }
        }
    }

    ShadowElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(plateFile ?: defaultPlateByte)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .height(64.dp)
            )

            Text(
                text = plateData.name ?: "",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = "(${plateData.normalText})",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PlatePage(
    onBackPressed: () -> Unit
) {
    val plateData = PlatePageViewModel.searchResult?.collectAsLazyPagingItems()

    fun search() {
        if (PlatePageViewModel.isSearchingActive && PlatePageViewModel.searchJob != null) {
            PlatePageViewModel.searchJob?.cancel()
        }

        PlatePageViewModel.searchJob = PlatePageViewModel.viewModelScope.launch(Dispatchers.IO) {
            PlatePageViewModel.isSearching = true
            PlatePageViewModel.isSearchingActive = true

            PlatePageViewModel.currentPage = 0

            PlatePageViewModel.search()

            PlatePageViewModel.searchJob = null
        }
    }

    if (PlatePageViewModel.listState == null) {
        PlatePageViewModel.listState = rememberLazyGridState()
    }

    LaunchedEffect(PlatePageViewModel.currentPage) {
        val page = PlatePageViewModel.currentPage
        val total = PlatePageViewModel.searchResultState.totalPage
        if (page >= 0 && page < total) {
            PlatePageViewModel.listState?.scrollToItem(0)
            PlatePageViewModel.search()
        }
    }

    LaunchedEffect(Unit) {
        if (!CollectionType.PLATE.manager!!.isLoaded) {
            PlatePageViewModel.viewModelScope.launch(Dispatchers.IO) {
                CollectionType.PLATE.manager!!.loadCollectionData()
                PlatePageViewModel.isLoaded = true
            }
        } else {
            PlatePageViewModel.isLoaded = true
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
                        modifier = Modifier.height(24.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = R.string.plate_page.getString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (!PlatePageViewModel.isLoaded) {
                UnknownProgressCircularProgress(
                    strokeWidth = 4.dp,
                    gapSize = 4.dp
                )
            }

            AnimatedVisibility(
                visible = PlatePageViewModel.isLoaded,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500)),
                label = "PlatePage"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CollapsibleSearchCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        isCollapsed = PlatePageViewModel.isSearchCardCollapsed,
                        onCollapseToggle = {
                            PlatePageViewModel.isSearchCardCollapsed = !PlatePageViewModel.isSearchCardCollapsed
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = PlatePageViewModel.searchText,
                                onValueChange = { PlatePageViewModel.searchText = it },
                                enabled = PlatePageViewModel.isLoaded,
                                label = { Text(R.string.search.getString()) },
                                placeholder = {
                                    Text(
                                        R.string.search_collection_placeholder.getString(),
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
                                enabled = PlatePageViewModel.isLoaded,
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp)
                                    .padding(4.dp)
                                    .weight(0.25f)
                            ) {
                                Text(
                                    text = R.string.search.getString(),
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
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .heightIn(1000.dp)
                                .padding(top = 8.dp),
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(
                                8.dp,
                                alignment = Alignment.Top
                            ),
                            state = PlatePageViewModel.listState!!
                        ) {
                            if (PlatePageViewModel.isSearchingActive) {
                                item(
                                    key = "search_bar",
                                    span = { GridItemSpan(2) }
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
                                                text = R.string.plate_search_result.getString()
                                                    .format(
                                                        PlatePageViewModel.searchResultState.currentSearchCount
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

                            if (plateData != null) {
                                items(
                                    count = plateData.itemCount,
                                    key = { plateData[it]?.id ?: it }
                                ) {
                                    val item = plateData[it]
                                    if (item != null) {
                                        PlateSimpleCard(item)
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
                            currentPage = PlatePageViewModel.currentPage + 1,
                            totalPage = PlatePageViewModel.searchResultState.totalPage
                        ) {
                            PlatePageViewModel.currentPage = it - 1
                        }
                    }
                }
            }
        }
    }
}