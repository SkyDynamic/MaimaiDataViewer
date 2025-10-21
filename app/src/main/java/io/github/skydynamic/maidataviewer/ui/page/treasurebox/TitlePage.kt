package io.github.skydynamic.maidataviewer.ui.page.treasurebox

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.data.MaimaiTitleData
import io.github.skydynamic.maidataviewer.core.getString
import io.github.skydynamic.maidataviewer.core.manager.TitleDataManager
import io.github.skydynamic.maidataviewer.ui.component.UnknownProgressCircularProgress
import io.github.skydynamic.maidataviewer.ui.component.WindowInsetsSpacer.TopPaddingSpacer
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object TitlePageViewModel : ViewModel() {
    var isLoaded by mutableStateOf(false)
    
    var searchText by mutableStateOf("")

    var filterRate by mutableIntStateOf(-1)

    var isSearching by mutableStateOf(false)

    var isSearchingActive by mutableStateOf(false)

    var searchJob by mutableStateOf<Job?>(null)

    var searchResult by mutableStateOf<List<MaimaiTitleData>>(emptyList())

    var listState by mutableStateOf<LazyListState?>(null)
}

fun search() {
    if (TitlePageViewModel.isSearchingActive && TitlePageViewModel.searchJob != null) {
        TitlePageViewModel.searchJob?.cancel()
    }

    TitlePageViewModel.searchJob = TitlePageViewModel.viewModelScope.launch(Dispatchers.IO) {
        TitlePageViewModel.isSearching = true
        TitlePageViewModel.isSearchingActive = true

        val rareType = if (TitlePageViewModel.filterRate == -1) null
        else TitlePageViewModel.filterRate

        TitlePageViewModel.searchResult = TitleDataManager.instance.search(
            TitlePageViewModel.searchText,
            rareType
        )
    }
}

@Composable
fun TitlePage(
    onBackPressed: () -> Unit
) {
    if (TitlePageViewModel.listState == null) {
        TitlePageViewModel.listState = rememberLazyListState()
    }

    LaunchedEffect(Unit) {
        if (!TitleDataManager.isLoaded()) {
            TitlePageViewModel.viewModelScope.launch(Dispatchers.IO) {
                TitleDataManager.instance.loadTitleData()
                TitlePageViewModel.isLoaded = true
            }
        } else {
            TitlePageViewModel.isLoaded = true
        }
    }

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopPaddingSpacer()

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
                    text = R.string.title_page.getString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
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
                                value = TitlePageViewModel.searchText,
                                onValueChange = { TitlePageViewModel.searchText = it },
                                enabled = TitlePageViewModel.isLoaded,
                                label = { Text(R.string.search.getString()) },
                                placeholder = {
                                    Text(
                                        R.string.search_placeholder.getString(),
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
                                    text = R.string.search.getString(),
                                    maxLines = 1,
                                    autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp)
                                )
                            }
                        }
                    }

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
                                                text = R.string.title_search_result.getString()
                                                    .format(
                                                        TitlePageViewModel.searchResult.size
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

                            if (TitlePageViewModel.searchResult.isNotEmpty()) {
                                items(
                                    TitlePageViewModel.searchResult,
                                    key = { it.id }
                                ) { title ->
                                    HorizontalDivider(
                                        thickness = 2.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(),
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
                    }
                }
            }
        }
    }
}