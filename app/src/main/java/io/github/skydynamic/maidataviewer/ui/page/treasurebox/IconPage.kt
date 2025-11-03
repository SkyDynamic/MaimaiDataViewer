package io.github.skydynamic.maidataviewer.ui.page.treasurebox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.items
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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.data.MaimaiIconData
import io.github.skydynamic.maidataviewer.core.getString
import io.github.skydynamic.maidataviewer.core.manager.collection.CollectionType
import io.github.skydynamic.maidataviewer.core.manager.resource.ResourceManagerType
import io.github.skydynamic.maidataviewer.ui.component.UnknownProgressCircularProgress
import io.github.skydynamic.maidataviewer.ui.component.WindowInsetsSpacer
import io.github.skydynamic.maidataviewer.ui.component.WindowInsetsSpacer.TopPaddingSpacer
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

object IconPageViewModel : ViewModel() {
    var isLoaded by mutableStateOf(false)

    var searchText by mutableStateOf("")

    var filterGenre by mutableIntStateOf(-1)

    var isSearching by mutableStateOf(false)

    var isSearchingActive by mutableStateOf(false)

    var searchJob by mutableStateOf<Job?>(null)

    var searchResult by mutableStateOf<List<MaimaiIconData>>(emptyList())

    var listState by mutableStateOf<LazyGridState?>(null)
}

@Composable
fun IconSimpleCard(
    iconData: MaimaiIconData
) {
    val iconResManager = ResourceManagerType.ICON.instance!!

    val defaultIconByte = remember { iconResManager.getResByteFromAssets(0) }
    var iconFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(iconData.id) {
        IconPageViewModel.viewModelScope.launch(Dispatchers.IO) {
            iconFile = try {
                ResourceManagerType.ICON.instance!!.getResFile(iconData.id)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(iconFile ?: defaultIconByte)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .height(64.dp)
            )

            Text(
                text = iconData.name ?: "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "(${iconData.normalText})",
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun IconPage(
    onBackPressed: () -> Unit
) {
    fun search() {
        if (IconPageViewModel.isSearchingActive && IconPageViewModel.searchJob != null) {
            IconPageViewModel.searchJob?.cancel()
        }

        IconPageViewModel.searchJob = IconPageViewModel.viewModelScope.launch(Dispatchers.IO) {
            IconPageViewModel.isSearching = true
            IconPageViewModel.isSearchingActive = true

            val rareType = if (IconPageViewModel.filterGenre == -1) null
            else IconPageViewModel.filterGenre

            IconPageViewModel.searchResult = CollectionType.ICON
                .getTypedManager<MaimaiIconData>()?.search(
                    IconPageViewModel.searchText
                ) { list ->
                    if (rareType != null) {
                        list.filter {
                            it.genre == rareType
                        }
                    } else {
                        list
                    }
                } ?: emptyList()
        }
    }

    if (IconPageViewModel.listState == null) {
        IconPageViewModel.listState = rememberLazyGridState()
    }

    LaunchedEffect(Unit) {
        if (!CollectionType.ICON.manager!!.isLoaded) {
            IconPageViewModel.viewModelScope.launch(Dispatchers.IO) {
                CollectionType.ICON.manager!!.loadCollectionData()
                IconPageViewModel.isLoaded = true
            }
        } else {
            IconPageViewModel.isLoaded = true
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
                    text = R.string.icon_page.getString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (!IconPageViewModel.isLoaded) {
                UnknownProgressCircularProgress(
                    strokeWidth = 4.dp,
                    gapSize = 4.dp
                )
            }

            AnimatedVisibility(
                visible = IconPageViewModel.isLoaded,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500)),
                label = "IconPage"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    ShadowElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = IconPageViewModel.searchText,
                                onValueChange = { IconPageViewModel.searchText = it },
                                enabled = IconPageViewModel.isLoaded,
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
                                enabled = IconPageViewModel.isLoaded,
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

                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .heightIn(1000.dp)
                            .padding(top = 8.dp),
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        state = IconPageViewModel.listState!!
                    ) {
                        if (IconPageViewModel.isSearchingActive) {
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
                                            text = R.string.icon_search_result.getString()
                                                .format(
                                                    IconPageViewModel.searchResult.size
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

                        if (IconPageViewModel.searchResult.isNotEmpty()) {
                            items(
                                items = IconPageViewModel.searchResult,
                                key = { it.id }
                            ) {
                                IconSimpleCard(it)
                            }
                        }
                    }
                }
            }
        }
    }
}