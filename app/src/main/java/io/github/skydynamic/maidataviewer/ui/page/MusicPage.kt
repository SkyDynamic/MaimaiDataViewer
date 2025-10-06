package io.github.skydynamic.maidataviewer.ui.page

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.core.getString
import io.github.skydynamic.maidataviewer.core.manager.GenreType
import io.github.skydynamic.maidataviewer.core.manager.MaiGenreManager
import io.github.skydynamic.maidataviewer.core.manager.MusicAliasManager
import io.github.skydynamic.maidataviewer.core.manager.MusicDataManager
import io.github.skydynamic.maidataviewer.ui.component.UnknownProgressCircularProgress
import io.github.skydynamic.maidataviewer.ui.component.card.MusicSimpleCard
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard
import io.github.skydynamic.maidataviewer.viewmodel.GlobalViewModel
import io.github.skydynamic.maidataviewer.viewmodel.SongPageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun search() {
    if (SongPageViewModel.isSearchingActive.value
        && SongPageViewModel.searchJob.value != null
    ) {
        SongPageViewModel.searchJob.value?.cancel()
    }
    SongPageViewModel.searchJob.value = GlobalViewModel.viewModelScope
        .launch(Dispatchers.IO) {
            SongPageViewModel.isSearchingActive.value = true
            SongPageViewModel.isSearching.value = true
            SongPageViewModel.shouldScrollToTop.value = true

            val result = mutableListOf<MaimaiMusicData>()

            val genre =
                if (SongPageViewModel.genreFilter.intValue == -1) null
                else SongPageViewModel.genreFilter.intValue
            val version =
                if (SongPageViewModel.versionFilter.intValue == -1) null
                else SongPageViewModel.versionFilter.intValue
            result += MusicDataManager.instance
                .searchMusicData(
                    SongPageViewModel.searchText.value,
                    genre,
                    version
                )
            MusicAliasManager.getMusicByAlias(
                SongPageViewModel.searchText.value,
                genre,
                version
            ).forEach {
                if (!result.contains(it)) result.add(it)
            }

            SongPageViewModel.searchResult.value = result
            SongPageViewModel.isSearchingActive.value = false
            SongPageViewModel.searchJob.value = null
        }
}


@Composable
private fun GenreDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelectedChange: (Int) -> Unit,
    genreType: GenreType
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .padding(4.dp)
            .heightIn(max = 400.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        DropdownMenuItem(
            text = { Text(R.string.all.getString()) },
            onClick = {
                onSelectedChange(-1)
                onDismissRequest()
            }
        )
        MaiGenreManager.get(genreType)
            .getGenreData()
            .forEach {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onSelectedChange(it.id)
                        onDismissRequest()
                    }
                )
            }
    }
}


@Composable
private fun GenreSelector(
    value: String,
    label: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 16.sp)
            )
            if (value.isNotEmpty()) {
                Text(
                    text = value.replace(
                        "plus",
                        "+",
                        true
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MusicPage(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onCardClick: (MaimaiMusicData) -> Unit
) {
    var genreDropdownMenuActive by remember { mutableStateOf(false) }
    var versionDropdownMenuActive by remember { mutableStateOf(false) }

    var showMessageCard by remember { mutableStateOf(false) }

    var loadedMusicCount by remember { mutableIntStateOf(0) }

    if (SongPageViewModel.listState.value == null) {
        SongPageViewModel.listState.value = rememberLazyListState()
    }

    val listState = SongPageViewModel.listState.value

    val activeItems = remember { mutableStateMapOf<Int, MaimaiMusicData?>() }

    LaunchedEffect(SongPageViewModel.searchResult.value) {
        if (SongPageViewModel.shouldScrollToTop.value) {
            listState?.scrollToItem(0)
            SongPageViewModel.shouldScrollToTop.value = false
        }

        activeItems.clear()

        val initCount = minOf(25, SongPageViewModel.searchResult.value.size)
        for (i in 0 until initCount) {
            activeItems[i] = SongPageViewModel.searchResult.value.getOrNull(i)
        }
    }


    LaunchedEffect(listState) {
        snapshotFlow {
            val first = listState?.firstVisibleItemIndex ?: 0
            Pair(first, first + (listState?.layoutInfo?.visibleItemsInfo?.size ?: 0))
        }
            .collect { (start, end) ->
                if (SongPageViewModel.searchResult.value.isEmpty()) {
                    activeItems.clear()
                    return@collect
                }

                val rangeStart = maxOf(0, start - 25)
                val rangeEnd = minOf(SongPageViewModel.searchResult.value.size, end + 25)

                activeItems.keys.removeAll { it !in rangeStart..rangeEnd }

                for (i in rangeStart until rangeEnd) {
                    if (!activeItems.containsKey(i)) {
                        activeItems[i] = SongPageViewModel.searchResult.value.getOrNull(i)
                    }
                }
            }
    }


    LaunchedEffect(SongPageViewModel.showLoadedFinishedMessage.value) {
        if (SongPageViewModel.showLoadedFinishedMessage.value) {
            showMessageCard = true
            launch {
                delay(1000)
                showMessageCard = false
                delay(300)
                SongPageViewModel.showLoadedFinishedMessage.value = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!MusicDataManager.instance.getIsLoaded()) {
            SongPageViewModel.viewModelScope.launch(Dispatchers.IO) {
                MusicDataManager.instance.loadMusicData {
                    SongPageViewModel.isLoadingMusic.value = false
                    loadedMusicCount = it
                    if (it > 0) {
                        SongPageViewModel.showLoadedFinishedMessage.value = true
                    }
                }
            }
        }

        if (!MusicAliasManager.instance.getIsLoaded()) {
            SongPageViewModel.viewModelScope.launch(Dispatchers.IO) {
                MusicAliasManager.instance.loadAliasData()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        if (SongPageViewModel.isLoadingMusic.value || SongPageViewModel.showLoadedFinishedMessage.value) {
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
                            text = R.string.load_finish.getString().format(loadedMusicCount),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (SongPageViewModel.isLoadingMusic.value) {
                    UnknownProgressCircularProgress(
                        strokeWidth = 4.dp,
                        gapSize = 4.dp
                    )
                }
            }
        } else if (SongPageViewModel.isSearching.value) {
            if (SongPageViewModel.isSearchingActive.value) {
                UnknownProgressCircularProgress(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    strokeWidth = 4.dp,
                    gapSize = 4.dp
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .heightIn(1000.dp)
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        state = listState!!
                    ) {
                        item("spacerTop") {
                            Spacer(Modifier.height(160.dp))
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
                                        text = R.string.search_result.getString().format(
                                            SongPageViewModel.searchResult.value.size
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

                        if (SongPageViewModel.searchResult.value.isNotEmpty()) {
                            items(
                                count = SongPageViewModel.searchResult.value.size,
                                key = { SongPageViewModel.searchResult.value[it].id }
                            ) { index ->
                                val item = SongPageViewModel.searchResult.value[index]

                                if (activeItems.contains(index)) {
                                    MusicSimpleCard(
                                        sharedTransitionScope,
                                        animatedContentScope,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 60.dp)
                                            .padding(top = 6.dp, bottom = 6.dp),
                                        music = item,
                                        onClick = {
                                            onCardClick(item)
                                        }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(60.dp))
                                }
                            }
                        }

                        item("spacerButton") {
                            Spacer(Modifier.height(90.dp))
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShadowElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = SongPageViewModel.searchText.value,
                        onValueChange = { SongPageViewModel.searchText.value = it },
                        enabled = !SongPageViewModel.isLoadingMusic.value,
                        label = { Text(R.string.search.getString()) },
                        placeholder = {
                            Text(
                                R.string.search_placeholder.getString(),
                                autoSize = TextAutoSize.StepBased(minFontSize = 8.sp)
                            ) },
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
                        enabled = !SongPageViewModel.isLoadingMusic.value,
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
                        GenreSelector(
                            value = MaiGenreManager.get(GenreType.MUSIC)
                                .getGenreName(SongPageViewModel.genreFilter.intValue),
                            label = R.string.genre.getString(),
                            onClick = { genreDropdownMenuActive = true }
                        )

                        GenreDropdownMenu(
                            expanded = genreDropdownMenuActive,
                            onDismissRequest = { genreDropdownMenuActive = false },
                            onSelectedChange = {
                                SongPageViewModel.genreFilter.intValue = it
                                if (SongPageViewModel.searchText.value != ""
                                    || SongPageViewModel.isSearching.value
                                ) {
                                    search()
                                }
                            },
                            genreType = GenreType.MUSIC,
                        )
                    }

                    Box(modifier = Modifier.weight(0.5f)) {
                        GenreSelector(
                            value = MaiGenreManager.get(GenreType.VERSION)
                                .getGenreName(SongPageViewModel.versionFilter.intValue),
                            label = R.string.versionGenre.getString(),
                            onClick = { versionDropdownMenuActive = true }
                        )

                        GenreDropdownMenu(
                            expanded = versionDropdownMenuActive,
                            onDismissRequest = { versionDropdownMenuActive = false },
                            onSelectedChange = {
                                SongPageViewModel.versionFilter.intValue = it
                                if (SongPageViewModel.searchText.value != ""
                                    || SongPageViewModel.isSearching.value
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
