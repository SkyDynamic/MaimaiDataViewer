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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.manager.resource.ResourceManagerType
import io.github.skydynamic.maidataviewer.core.strings
import io.github.skydynamic.maidataviewer.ui.component.UnknownProgressCircularProgress
import io.github.skydynamic.maidataviewer.ui.component.card.CollapsibleSearchCard
import io.github.skydynamic.maidataviewer.ui.component.card.CollectionSimpleCard
import io.github.skydynamic.maidataviewer.ui.component.card.PaginationCard
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SearchBarContent(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    isLoaded: Boolean,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            enabled = isLoaded,
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
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(65.dp)
                .weight(0.75f)
        )

        Button(
            onClick = onSearch,
            enabled = isLoaded,
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

@Composable
fun SearchResultsHeader(
    resultCount: Int,
    resultText: String
) {
    ShadowElevatedCard(
        modifier = Modifier
            .heightIn(max = 40.dp)
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = resultText.format(resultCount),
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


@Composable
fun CollectionPage(
    viewModel: BaseCollectionViewModel,
    resourceManager: ResourceManagerType,
    title: String,
    searchResultStringRes: String,
    onBackPressed: () -> Unit,
    onPicked: Boolean = false,
) {
    val onPickedFunction = if (onPicked) {
        { file: File? ->
            viewModel.pickItemFile = file
            onBackPressed()
        }
    } else {
        null
    }

    val collectionData = viewModel.searchResult?.collectAsLazyPagingItems()

    fun search() {
        if (viewModel.isSearchingActive && viewModel.searchJob != null) {
            viewModel.searchJob?.cancel()
        }

        viewModel.searchJob = viewModel.viewModelScope.launch(Dispatchers.IO) {
            viewModel.isSearching = true
            viewModel.isSearchingActive = true
            viewModel.currentPage = 0
            viewModel.search()
            viewModel.searchJob = null
        }
    }

    if (viewModel.listState == null) {
        viewModel.listState = rememberLazyGridState()
    }

    LaunchedEffect(viewModel.currentPage) {
        val page = viewModel.currentPage
        val total = viewModel.searchResultState.totalPage
        if (page >= 0 && page < total) {
            viewModel.listState?.scrollToItem(0)
            viewModel.search()
        }
    }

    LaunchedEffect(Unit) {
        if (!viewModel.getCollectionType().manager!!.isLoaded) {
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                viewModel.getCollectionType().manager!!.loadCollectionData()
                viewModel.isLoaded = true
            }
        } else {
            viewModel.isLoaded = true
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
                IconButton(onClick = onBackPressed) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = Icons.Default.Close.name,
                        modifier = Modifier.height(24.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (!viewModel.isLoaded) {
                UnknownProgressCircularProgress(
                    strokeWidth = 4.dp,
                    gapSize = 4.dp
                )
            }

            AnimatedVisibility(
                visible = viewModel.isLoaded,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500)),
                label = "CollectionPage"
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    CollapsibleSearchCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        isCollapsed = viewModel.isSearchCardCollapsed,
                        onCollapseToggle = {
                            viewModel.isSearchCardCollapsed = !viewModel.isSearchCardCollapsed
                        }
                    ) {
                        SearchBarContent(
                            searchText = viewModel.searchText,
                            onSearchTextChange = { viewModel.searchText = it },
                            isLoaded = viewModel.isLoaded,
                            onSearch = { search() }
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxSize()
                                .heightIn(1000.dp)
                                .padding(top = 8.dp),
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            state = viewModel.listState!!
                        ) {
                            if (viewModel.isSearchingActive) {
                                item(key = "search_bar", span = { GridItemSpan(2) }) {
                                    SearchResultsHeader(
                                        resultCount = viewModel.searchResultState.currentSearchCount,
                                        resultText = searchResultStringRes
                                    )
                                }
                            }

                            if (collectionData != null) {
                                items(
                                    count = collectionData.itemCount,
                                    key = { collectionData[it]?.id ?: it }
                                ) { index ->
                                    val item = collectionData[index]
                                    if (item != null) {
                                        CollectionSimpleCard(
                                            manager = resourceManager.instance!!,
                                            collectionData = item,
                                            picked = onPickedFunction
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }

                                if (collectionData.itemCount % 2 != 0) {
                                    item(key = "spacer") {
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }
                            }

                            item(key = "spacerBottom") {
                                Spacer(modifier = Modifier.height(65.dp))
                            }
                        }

                        PaginationCard(
                            modifier = Modifier
                                .height(70.dp)
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .align(Alignment.BottomCenter),
                            currentPage = viewModel.currentPage + 1,
                            totalPage = viewModel.searchResultState.totalPage
                        ) { page ->
                            viewModel.currentPage = page - 1
                        }
                    }
                }
            }
        }
    }
}
