package io.github.skydynamic.maidataviewer.ui.component.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.core.getString
import io.github.skydynamic.maidataviewer.core.manager.GenreType
import io.github.skydynamic.maidataviewer.core.manager.MaiGenreManager
import io.github.skydynamic.maidataviewer.core.manager.MaimaiJacketManager
import io.github.skydynamic.maidataviewer.core.manager.MusicDataManager
import io.github.skydynamic.maidataviewer.core.noRippleClickable
import io.github.skydynamic.maidataviewer.ui.component.UnknownProgressCircularProgress
import io.github.skydynamic.maidataviewer.ui.component.button.GenreSelectorButton
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard
import io.github.skydynamic.maidataviewer.ui.component.menu.GenreDropdownMenu
import io.github.skydynamic.maidataviewer.viewmodel.GlobalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun RandomMusicItem(
    id: Int,
    isRolling: Boolean = false,
    onCardClick: (MaimaiMusicData) -> Unit,
) {
    val defaultJacketFile = remember { MaimaiJacketManager.instance.getJacketFromAssets(0) }
    var jacketFile by remember { mutableStateOf<File?>(null) }
    val musicData: MaimaiMusicData? = MusicDataManager.getMusicData(id)

    LaunchedEffect(id) {
        GlobalViewModel.viewModelScope.launch(Dispatchers.IO) {
            jacketFile = try {
                MaimaiJacketManager.instance.getJacketFile(id)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        } else {
            Text(
                text = "",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun RandomMusicDialog(
    onCardClick: (MaimaiMusicData) -> Unit,
    onDismiss: () -> Unit
) {
    var genreFilter by remember { mutableIntStateOf(-1) }
    var versionFilter by remember { mutableIntStateOf(-1) }
    var minLevel by remember { mutableStateOf("") }
    var maxLevel by remember { mutableStateOf("") }
    var randomCount by remember { mutableStateOf("") }

    var genreDropdownMenuActive by remember { mutableStateOf(false) }
    var versionDropdownMenuActive by remember { mutableStateOf(false) }

    val lazyGridState = rememberLazyGridState()
    var isRolling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var randomMusicList by remember { mutableStateOf(listOf(0, 0, 0, 0)) }

    var isLoadingMusic by remember { mutableStateOf(true) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            scope.launch(Dispatchers.IO) {
                while (isRolling) {
                    val list = MusicDataManager.randomMusic(
                        genreId = if (genreFilter != -1) genreFilter else null,
                        versionId = if (versionFilter != -1) versionFilter else null,
                        minLevel = if (minLevel.isNotEmpty()) minLevel.toFloat() else 0.0F,
                        maxLevel = if (maxLevel.isNotEmpty()) maxLevel.toFloat() else 99.9F,
                        count = 4
                    ).map { it.id }.toMutableList()

                    while (list.size < 4) {
                        list.add(0)
                    }

                    randomMusicList = list

                    delay(200)
                }
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

    FullScreenDialog(
        title = R.string.random_music.getString(),
        onDismiss = onDismiss,
    ) {
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
                                onSelectedChange = { genreFilter = it },
                                genreType = GenreType.MUSIC,
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
                                onSelectedChange = { versionFilter = it },
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
                                        minLevel = it
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
                                        maxLevel = it
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
                                randomCount = it
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
                            onCardClick = onCardClick
                        )
                    }
                }
            }
        }
    }
}