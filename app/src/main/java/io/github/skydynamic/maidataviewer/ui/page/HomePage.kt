package io.github.skydynamic.maidataviewer.ui.page

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import io.github.skydynamic.maidataviewer.BuildConfig
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.MaimaiDataMeta
import io.github.skydynamic.maidataviewer.core.getString
import io.github.skydynamic.maidataviewer.core.manager.MusicDataManager
import io.github.skydynamic.maidataviewer.core.manager.TitleDataManager
import io.github.skydynamic.maidataviewer.core.manager.UpdateDataManager
import io.github.skydynamic.maidataviewer.core.not
import io.github.skydynamic.maidataviewer.ui.AppContent
import io.github.skydynamic.maidataviewer.ui.component.WindowInsetsSpacer
import io.github.skydynamic.maidataviewer.ui.component.card.IntroductionCard
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard
import io.github.skydynamic.maidataviewer.ui.component.text.AnimatedTextTitleGroup
import io.github.skydynamic.maidataviewer.ui.component.text.TextTitleGroup
import io.github.skydynamic.maidataviewer.viewmodel.GlobalViewModel
import io.github.skydynamic.maidataviewer.viewmodel.HomePageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    jumpFunction: (AppContent.Tab) -> Unit
) {
    LaunchedEffect(Unit) {
        MaimaiDataMeta.init {
            HomePageViewModel.isMetaLoading.value = false
        }
    }

    @Composable
    fun AppInfoCard(meta: MaimaiDataMeta) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextTitleGroup(
                title = R.string.app_version.getString(),
                titleStyle = MaterialTheme.typography.titleSmall,
                subtitle = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedTextTitleGroup(
                    title = R.string.current_maimai_data_version.getString(),
                    titleStyle = MaterialTheme.typography.titleSmall,
                    subtitle = "${meta.currentMaimaiDataVersion}" +
                            " (${meta.currentMaimaiDataVersion.toStandardString()})"
                )

                if (meta.currentMaimaiDataVersion.major == -1) {
                    Spacer(modifier = Modifier.weight(1f))
                    TooltipBox(
                        modifier = Modifier.padding(end = 16.dp),
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip { Text(R.string.not_local_mai_data.getString()) }
                        },
                        state = rememberTooltipState()
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            "",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                AnimatedTextTitleGroup(
                    title = R.string.latest_maimai_data_version.getString(),
                    titleStyle = MaterialTheme.typography.titleSmall,
                    subtitle = "${meta.latestMaimaiDataVersion}" +
                            " (${meta.latestMaimaiDataVersion.toStandardString()})",
                    subtitleColor = if (!meta.isLatestMaimaiDataVersion()) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    subtitleWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                if (!meta.isLatestMaimaiDataVersion() && !HomePageViewModel.isUpdateMaiDataAvailable) {
                    TextButton(
                        modifier = Modifier
                            .height(40.dp),
                        onClick = {
                            HomePageViewModel.isUpdateMaiDataAvailable.value = true
                            GlobalViewModel.viewModelScope.launch(Dispatchers.IO) {
                                UpdateDataManager.instance
                                    .updateData(meta.latestMaimaiDataVersion) {
                                        if (it != null) {
                                            meta.updateMaimaiData(
                                                it,
                                                meta.latestMaimaiDataVersion
                                            )
                                        }
                                        HomePageViewModel.isUpdateMaiDataAvailable.value = false
                                    }

                                if (MusicDataManager.instance.getIsLoaded()) {
                                    MusicDataManager.instance.reloadMusicData {
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Refresh, "")
                    }
                } else if (!meta.isLatestMaimaiDataVersion() && HomePageViewModel.isUpdateMaiDataAvailable.value) {
                    CircularProgressIndicator(
                        strokeWidth = 4.dp,
                        gapSize = 4.dp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                AnimatedTextTitleGroup(
                    title = R.string.title_version.getString(),
                    titleStyle = MaterialTheme.typography.titleSmall,
                    subtitle = "${meta.latestTitleDataVersion}" +
                            " (${meta.latestTitleDataVersion.toStandardString()})",
                    subtitleColor = if (!meta.isLatestTitleDataVersion()) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    subtitleWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                if (!meta.isLatestTitleDataVersion() && !HomePageViewModel.isUpdateMaiTitleDataAvailable) {
                    TextButton(
                        modifier = Modifier
                            .height(40.dp),
                        onClick = {
                            HomePageViewModel.isUpdateMaiTitleDataAvailable.value = true
                            GlobalViewModel.viewModelScope.launch(Dispatchers.IO) {
                                TitleDataManager.instance.downloadTitleData {
                                    if (it != null) {
                                        meta.updateTitleData(
                                            it,
                                            meta.latestTitleDataVersion
                                        )
                                    }
                                    HomePageViewModel.isUpdateMaiTitleDataAvailable.value = false
                                    TitleDataManager.instance.loadTitleData()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Refresh, "")
                    }
                } else if (!meta.isLatestTitleDataVersion() && HomePageViewModel.isUpdateMaiTitleDataAvailable.value) {
                    CircularProgressIndicator(
                        strokeWidth = 4.dp,
                        gapSize = 4.dp
                    )
                }
            }
        }
    }

    @Composable
    fun MainPart(
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            IntroductionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                title = R.string.home_songcard_title.getString(),
                subtitle = R.string.home_songcard_subtitle.getString()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text(
                        text = R.string.home_songcard_desc.getString()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.End)
                            .padding(top = 16.dp)
                    ) {
                        Button(
                            modifier = Modifier
                                .align(Alignment.Bottom),
                            onClick = {
                                jumpFunction(AppContent.Tab.Music)
                            }
                        ) {
                            Text(text = R.string.jump_to.getString())
                        }
                    }
                }
            }

            ShadowElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                AnimatedContent(
                    targetState = HomePageViewModel.isMetaLoading.value,
                    transitionSpec = {
                        slideInVertically(animationSpec = tween(220)) { height -> height } +
                                fadeIn(animationSpec = tween(220)) togetherWith
                                slideOutVertically(animationSpec = tween(220)) { height -> -height } +
                                fadeOut(animationSpec = tween(220))
                    },
                    label = "AppInfoTransition"
                ) { isLoading ->
                    if (isLoading) {
                        Column(
                            modifier = Modifier
                                .heightIn(min = 150.dp)
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            LoadingIndicator(
                                modifier = Modifier
                                    .size(64.dp)
                            )
                            Text(
                                text = R.string.loading.getString(),
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        val meta = MaimaiDataMeta.getInstance()
                        AppInfoCard(meta = meta)
                    }
                }
            }
        }
    }

    Row(
        Modifier
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            MainPart()
            WindowInsetsSpacer.BottomPaddingSpacer()
        }
    }
}
