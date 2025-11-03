package io.github.skydynamic.maidataviewer.ui.page.treasurebox

import android.view.Surface
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.core.getString
import io.github.skydynamic.maidataviewer.core.manager.GenreType
import io.github.skydynamic.maidataviewer.core.manager.MaiGenreManager
import io.github.skydynamic.maidataviewer.core.manager.MusicDataManager
import io.github.skydynamic.maidataviewer.core.manager.resource.ResourceManagerType
import io.github.skydynamic.maidataviewer.core.noRippleClickable
import io.github.skydynamic.maidataviewer.ui.AppNavController
import io.github.skydynamic.maidataviewer.ui.component.UnknownProgressCircularProgress
import io.github.skydynamic.maidataviewer.ui.component.WindowInsetsSpacer.TopPaddingSpacer
import io.github.skydynamic.maidataviewer.ui.component.button.GenreSelectorButton
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard
import io.github.skydynamic.maidataviewer.ui.component.menu.GenreDropdownMenu
import io.github.skydynamic.maidataviewer.viewmodel.GlobalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

object RandomMusicPageViewModel : ViewModel() {
    val randomMusicList = mutableStateOf(listOf(0, 0, 0, 0))

    val genreFilter = mutableIntStateOf(-1)
    val versionFilter = mutableIntStateOf(-1)
    val minLevel = mutableStateOf("")
    val maxLevel = mutableStateOf("")
    val randomCount = mutableStateOf("")
}

@Composable
fun RandomMusicItem(
    id: Int,
    isRolling: Boolean = false,
    onCardClick: (MaimaiMusicData) -> Unit,
) {
    val defaultJacketFile = remember { ResourceManagerType.JACKET.instance!!.getResByteFromAssets(0) }
    var jacketFile by remember { mutableStateOf<File?>(null) }
    val musicData: MaimaiMusicData? = MusicDataManager.getMusicData(id)

    LaunchedEffect(id) {
        GlobalViewModel.viewModelScope.launch(Dispatchers.IO) {
            jacketFile = try {
                ResourceManagerType.JACKET.instance!!.getResFile(id)
            } catch (_: Exception) {
                null
            }
        }
    }

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(8.dp)
            .noRippleClickable {
                if (musicData != null) {
                    onCardClick(musicData)
                }
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(jacketFile ?: defaultJacketFile)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        )

        val name = musicData?.name ?: ""
        val type = musicData?.id?.let {
            if (it >= 10000) "[DX]" else "[SD]"
        } ?: ""

        if (!isRolling) {
            Text(
                text = name + type,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground,
                overflow = TextOverflow.MiddleEllipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        } else {
            Text(
                text = "",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                overflow = TextOverflow.MiddleEllipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun RandomMusicPage(
    onBackPressed: () -> Unit
) {
    var genreFilter by remember { RandomMusicPageViewModel.genreFilter }
    var versionFilter by remember { RandomMusicPageViewModel.versionFilter }
    var minLevel by remember { RandomMusicPageViewModel.minLevel }
    var maxLevel by remember { RandomMusicPageViewModel.maxLevel }
    var randomCount by remember { RandomMusicPageViewModel.randomCount }

    var genreDropdownMenuActive by remember { mutableStateOf(false) }
    var versionDropdownMenuActive by remember { mutableStateOf(false) }

    val lazyGridState = rememberLazyGridState()
    var isRolling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var randomMusicList by remember { RandomMusicPageViewModel.randomMusicList }

    var isLoadingMusic by remember { mutableStateOf(true) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            scope.launch(Dispatchers.IO) {
                val finalList = MusicDataManager.randomMusic(
                    genreId = if (genreFilter != -1) genreFilter else null,
                    versionId = if (versionFilter != -1) versionFilter else null,
                    minLevel = if (minLevel.isNotEmpty()) minLevel.toFloat() else 0.0F,
                    maxLevel = if (maxLevel.isNotEmpty()) maxLevel.toFloat() else 99.9F,
                    count = 4
                ).map { it.id }.toMutableList()

                while (finalList.size < 4) {
                    finalList.add(0)
                }

                RandomMusicPageViewModel.randomMusicList.value = finalList

                val allMusicIds = MusicDataManager.instance.searchMusicData(
                    "",
                    genreId = if (genreFilter != -1) genreFilter else null,
                    versionId = if (versionFilter != -1) versionFilter else null
                ).map { it.id }

                val startTime = System.currentTimeMillis()
                val totalTime = 3000L

                while (isRolling && System.currentTimeMillis() - startTime < totalTime) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val progress = elapsed.toFloat() / totalTime

                    val tempList = if (progress < 0.8f) {
                        List(4) {
                            if (allMusicIds.isNotEmpty()) {
                                allMusicIds.random()
                            } else {
                                (0..10000).random()
                            }
                        }
                    } else if (progress < 0.95f) {
                        List(4) { index ->
                            if (index < 2) {
                                finalList[index]
                            } else {
                                if (allMusicIds.isNotEmpty()) {
                                    allMusicIds.random()
                                } else {
                                    (0..10000).random()
                                }
                            }
                        }
                    } else {
                        finalList
                    }

                    RandomMusicPageViewModel.randomMusicList.value = tempList

                    val delayTime = when {
                        progress < 0.1f -> 100L
                        progress < 0.3f -> 150L
                        progress < 0.5f -> 200L
                        progress < 0.7f -> 250L
                        progress < 0.9f -> 300L
                        else -> 350L
                    }

                    delay(delayTime)
                }

                RandomMusicPageViewModel.randomMusicList.value = finalList
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!MusicDataManager.instance.getIsLoaded()) {
            scope.launch(Dispatchers.IO) {
                MusicDataManager.instance.loadMusicData(onFinished = {
                    isLoadingMusic = false
                })
            }
        } else {
            isLoadingMusic = false
        }
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
                    text = R.string.random_music.getString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (isLoadingMusic) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    UnknownProgressCircularProgress(
                        strokeWidth = 4.dp,
                        gapSize = 4.dp
                    )
                }
            } else {
                ShadowElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(0.5f)) {
                                GenreSelectorButton(
                                    value = MaiGenreManager.musicGenre.getGenreName(genreFilter),
                                    label = R.string.genre.getString(),
                                    onClick = { genreDropdownMenuActive = true }
                                )

                                GenreDropdownMenu(
                                    expanded = genreDropdownMenuActive,
                                    onDismissRequest = { genreDropdownMenuActive = false },
                                    onSelectedChange = {
                                        RandomMusicPageViewModel.genreFilter.intValue = it
                                    },
                                    genreType = GenreType.MUSIC,
                                    excludeStage = true
                                )
                            }

                            Box(modifier = Modifier.weight(0.5f)) {
                                GenreSelectorButton(
                                    value = MaiGenreManager.versionGenre.getGenreName(versionFilter),
                                    label = R.string.versionGenre.getString(),
                                    onClick = { versionDropdownMenuActive = true }
                                )

                                GenreDropdownMenu(
                                    expanded = versionDropdownMenuActive,
                                    onDismissRequest = { versionDropdownMenuActive = false },
                                    onSelectedChange = {
                                        RandomMusicPageViewModel.versionFilter.intValue = it
                                    },
                                    genreType = GenreType.VERSION,
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    text = R.string.min_level.getString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                )

                                OutlinedTextField(
                                    value = minLevel,
                                    onValueChange = {
                                        if (it.toFloatOrNull() != null
                                            && it.toFloat() <= 99f
                                            && it.toFloat() >= 0f
                                            || it.isEmpty()
                                        ) {
                                            RandomMusicPageViewModel.minLevel.value = it
                                        }
                                    },
                                    modifier = Modifier
                                        .height(56.dp)
                                        .fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Decimal
                                    ),
                                    placeholder = {
                                        Text(
                                            text = R.string.rating_calculator_music_level_placeholder.getString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Text(
                                    text = R.string.max_level.getString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                )

                                OutlinedTextField(
                                    value = maxLevel,
                                    onValueChange = {
                                        if (it.toFloatOrNull() != null
                                            && it.toFloat() <= 99f
                                            && it.toFloat() >= 0f
                                            || it.isEmpty()
                                        ) {
                                            RandomMusicPageViewModel.maxLevel.value = it
                                        }
                                    },
                                    modifier = Modifier
                                        .height(56.dp)
                                        .fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Decimal
                                    ),
                                    placeholder = {
                                        Text(
                                            text = R.string.rating_calculator_music_level_placeholder.getString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                )
                            }
                        }

                        Text(
                            text = R.string.random_count.getString(),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )

                        OutlinedTextField(
                            value = randomCount,
                            onValueChange = {
                                if (it.toIntOrNull() != null
                                    && it.toInt() <= 4f
                                    && it.toInt() > 0f
                                    || it.isEmpty()
                                ) {
                                    RandomMusicPageViewModel.randomCount.value = it
                                }
                            },
                            modifier = Modifier
                                .height(56.dp)
                                .fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Decimal
                            ),
                            placeholder = {
                                Text(
                                    text = R.string.random_count_placeholder.getString()
                                        .format("1...4"),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        )

                        Button(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    isRolling = true
                                    delay(3000)
                                    isRolling = false
                                }
                            },
                            modifier = Modifier
                                .padding(top = 8.dp),
                            enabled = !isRolling
                        ) {
                            Text(
                                text = R.string.random_music.getString(),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }

                val count = if (randomCount.isEmpty()) {
                    4
                } else {
                    randomCount.toInt()
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (count >= 2) 2 else 1),
                    state = lazyGridState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(count) { index ->
                        AnimatedContent(
                            targetState = randomMusicList[index],
                            label = "RandomMusicItem",
                            transitionSpec = {
                                (slideInVertically { it } + fadeIn()).togetherWith(
                                    slideOutVertically { -it } + fadeOut())
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) { targetState ->
                            RandomMusicItem(
                                id = targetState,
                                isRolling = isRolling,
                                onCardClick = {
                                    AppNavController.getInstance()
                                        .navigate("musicDetail/$targetState")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}