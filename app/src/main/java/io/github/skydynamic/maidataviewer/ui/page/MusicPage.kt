package io.github.skydynamic.maidataviewer.ui.page

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.strings
import io.github.skydynamic.maidataviewer.core.manager.GenreType
import io.github.skydynamic.maidataviewer.core.manager.MaiGenreManager
import io.github.skydynamic.maidataviewer.core.manager.MusicAliasManager
import io.github.skydynamic.maidataviewer.core.manager.MusicDataManager
import io.github.skydynamic.maidataviewer.ui.AppNavController
import io.github.skydynamic.maidataviewer.ui.component.UnknownProgressCircularProgress
import io.github.skydynamic.maidataviewer.ui.component.button.GenreSelectorButton
import io.github.skydynamic.maidataviewer.ui.component.card.CollapsibleSearchCard
import io.github.skydynamic.maidataviewer.ui.component.card.MusicSimpleCard
import io.github.skydynamic.maidataviewer.ui.component.card.PaginationCard
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard
import io.github.skydynamic.maidataviewer.ui.component.menu.GenreDropdownMenu
import io.github.skydynamic.maidataviewer.viewmodel.GlobalViewModel
import io.github.skydynamic.maidataviewer.viewmodel.MusicPageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun search() {
    if (MusicPageViewModel.isSearchingActive.value
        && MusicPageViewModel.searchJob.value != null
    ) {
        MusicPageViewModel.searchJob.value?.cancel()
    }

    MusicPageViewModel.searchJob.value = GlobalViewModel.viewModelScope
        .launch(Dispatchers.IO) {
            MusicPageViewModel.isSearchingActive.value = true
            MusicPageViewModel.isSearching.value = true
            MusicPageViewModel.shouldScrollToTop.value = true

            MusicPageViewModel.currentPage.intValue = 0

            MusicPageViewModel.search()

            MusicPageViewModel.isSearchingActive.value = false
            MusicPageViewModel.searchJob.value = null
        }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MusicPage() {
    var genreDropdownMenuActive by remember { mutableStateOf(false) }
    var versionDropdownMenuActive by remember { mutableStateOf(false) }

    var showMessageCard by remember { mutableStateOf(false) }

    if (MusicPageViewModel.listState.value == null) {
        MusicPageViewModel.listState.value = rememberLazyListState()
    }

    val listState = MusicPageViewModel.listState.value

    val musicData = MusicPageViewModel.searchResult.value?.collectAsLazyPagingItems()

    LaunchedEffect(MusicPageViewModel.currentPage.intValue) {
        val page = MusicPageViewModel.currentPage.intValue
        val total = MusicPageViewModel.searchResultState.value.totalPage
        if (page >= 0 && page < total) {
            listState?.scrollToItem(0)

            MusicPageViewModel.search()
        }
    }

    LaunchedEffect(MusicPageViewModel.showLoadedFinishedMessage.value) {
        if (MusicPageViewModel.showLoadedFinishedMessage.value) {
            showMessageCard = true
            launch {
                delay(1000)
                showMessageCard = false
                delay(300)
                MusicPageViewModel.showLoadedFinishedMessage.value = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!MusicDataManager.instance.getIsLoaded()) {
            MusicPageViewModel.viewModelScope.launch(Dispatchers.IO) {
                MusicDataManager.instance.loadMusicData {
                    MusicPageViewModel.isLoadingMusic.value = false
                    if (it > 0) {
                        MusicPageViewModel.showLoadedFinishedMessage.value = true
                    }
                }
            }
        } else {
            MusicPageViewModel.isLoadingMusic.value = false
        }

        if (!MusicAliasManager.instance.getIsLoaded()) {
            MusicPageViewModel.viewModelScope.launch(Dispatchers.IO) {
                MusicAliasManager.instance.loadAliasData()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        if (MusicPageViewModel.isLoadingMusic.value || MusicPageViewModel.showLoadedFinishedMessage.value) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = showMessageCard,
                    enter = expandHorizontally(
                        animationSpec = tween(durationMillis = 500)
                    ),
                    exit = shrinkHorizontally(
                        animationSpec = tween(durationMillis = 500)
                    )
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = R.string.load_finish.strings.format(MusicDataManager.getMusicLoadedSize()),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (MusicPageViewModel.isLoadingMusic.value) {
                    UnknownProgressCircularProgress(
                        strokeWidth = 4.dp,
                        gapSize = 4.dp
                    )
                }
            }
        } else if (MusicPageViewModel.isSearching.value) {
            if (MusicPageViewModel.isSearchingActive.value) {
                UnknownProgressCircularProgress(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    strokeWidth = 4.dp,
                    gapSize = 4.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(top = 8.dp)
                        .padding(bottom = 65.dp),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .heightIn(1000.dp)
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        state = listState!!
                    ) {
                        item("spacerTop") {
                            AnimatedContent(
                                targetState = MusicPageViewModel.isSearchCardCollapsed.value,
                                transitionSpec = {
                                    if (targetState) {
                                        slideInVertically { height -> height } + fadeIn() togetherWith
                                                slideOutVertically { height -> -height } + fadeOut()
                                    } else {
                                        slideInVertically { height -> -height } + fadeIn() togetherWith
                                                slideOutVertically { height -> height } + fadeOut()
                                    }
                                }
                            ) { isCollapsed ->
                                Spacer(
                                    modifier = Modifier
                                        .height(
                                            if (!isCollapsed) {
                                                190.dp
                                            } else {
                                                12.dp
                                            }
                                        )
                                )
                            }
                        }

                        item(
                            key = "search_result_title"
                        ) {
                            ShadowElevatedCard(
                                modifier = Modifier
                                    .heightIn(max = 40.dp)
                                    .fillMaxSize()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = R.string.search_result.strings.format(
                                            MusicPageViewModel.searchResultState.value.currentSearchCount
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
                        }

                        if (musicData != null) {
                            items(
                                count = musicData.itemCount,
                                key = musicData.itemKey { it.id }
                            ) { index ->
                                val item = musicData[index]

                                if (item != null) {
                                    MusicSimpleCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 60.dp)
                                            .padding(top = 6.dp, bottom = 6.dp),
                                        music = item,
                                        onClick = {
                                            AppNavController.getInstance()
                                                .navigate("musicDetail/${item.id}")
                                        }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(60.dp))
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
                            .padding(vertical = 8.dp)
                            .align(Alignment.BottomCenter),
                        currentPage = MusicPageViewModel.currentPage.intValue + 1,
                        totalPage = MusicPageViewModel.searchResultState.value.totalPage,
                    ) {
                        MusicPageViewModel.currentPage.intValue = it - 1
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CollapsibleSearchCard(
                modifier = Modifier
                    .fillMaxWidth(),
                isCollapsed = MusicPageViewModel.isSearchCardCollapsed.value,
                onCollapseToggle = {
                    MusicPageViewModel.isSearchCardCollapsed.value =
                        !MusicPageViewModel.isSearchCardCollapsed.value
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = MusicPageViewModel.searchText.value,
                        onValueChange = { MusicPageViewModel.searchText.value = it },
                        enabled = !MusicPageViewModel.isLoadingMusic.value,
                        label = { Text(R.string.search.strings) },
                        placeholder = {
                            Text(
                                R.string.search_placeholder.strings,
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
                        enabled = !MusicPageViewModel.isLoadingMusic.value,
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

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    thickness = 2.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(0.5f)) {
                        GenreSelectorButton(
                            value = MaiGenreManager.musicGenre
                                .getGenreName(MusicPageViewModel.genreFilter.intValue),
                            label = R.string.genre.strings,
                            onClick = { genreDropdownMenuActive = true }
                        )

                        GenreDropdownMenu(
                            expanded = genreDropdownMenuActive,
                            onDismissRequest = { genreDropdownMenuActive = false },
                            onSelectedChange = {
                                MusicPageViewModel.genreFilter.intValue = it
                                if (MusicPageViewModel.searchText.value != ""
                                    || MusicPageViewModel.isSearching.value
                                ) {
                                    search()
                                }
                            },
                            genreType = GenreType.MUSIC,
                        )
                    }

                    Box(modifier = Modifier.weight(0.5f)) {
                        GenreSelectorButton(
                            value = MaiGenreManager.versionGenre
                                .getGenreName(MusicPageViewModel.versionFilter.intValue),
                            label = R.string.versionGenre.strings,
                            onClick = { versionDropdownMenuActive = true }
                        )

                        GenreDropdownMenu(
                            expanded = versionDropdownMenuActive,
                            onDismissRequest = { versionDropdownMenuActive = false },
                            onSelectedChange = {
                                MusicPageViewModel.versionFilter.intValue = it
                                if (MusicPageViewModel.searchText.value != ""
                                    || MusicPageViewModel.isSearching.value
                                ) {
                                    search()
                                }
                            },
                            genreType = GenreType.VERSION,
                        )
                    }
                }
            }
        }
    }
}