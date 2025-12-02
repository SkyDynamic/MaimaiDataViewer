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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.material3.TooltipAnchorPosition
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
import io.github.skydynamic.maidataviewer.core.manager.MusicDataManager
import io.github.skydynamic.maidataviewer.core.manager.UpdateDataManager
import io.github.skydynamic.maidataviewer.core.not
import io.github.skydynamic.maidataviewer.core.strings
import io.github.skydynamic.maidataviewer.ui.AppContent
import io.github.skydynamic.maidataviewer.ui.AppNavController
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
                title = R.string.app_version.strings,
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
                    title = R.string.current_maimai_data_version.strings,
                    titleStyle = MaterialTheme.typography.titleSmall,
                    subtitle = "${meta.currentMaimaiDataVersion}" +
                            " (${meta.currentMaimaiDataVersion.toStandardString()})"
                )

                if (meta.currentMaimaiDataVersion.major == -1) {
                    Spacer(modifier = Modifier.weight(1f))
                    TooltipBox(
                        modifier = Modifier.padding(end = 16.dp),
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above
                        ),
                        tooltip = {
                            PlainTooltip { Text(R.string.not_local_mai_data.strings) }
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
                    title = R.string.latest_maimai_data_version.strings,
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
        }
    }

    fun LazyListScope.mainPart() {
        item {
            IntroductionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                title = R.string.home_songcard_title.strings,
                subtitle = R.string.home_songcard_subtitle.strings
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text(
                        text = R.string.home_songcard_desc.strings
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
                            Text(text = R.string.jump_to.strings)
                        }
                    }
                }
            }

            IntroductionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                title = R.string.data_manager_page.strings,
                subtitle = R.string.data_manager_page_desc.strings
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text(
                        text = R.string.data_manager_page_desc_content.strings
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
                                AppNavController.getInstance().navigate("dataManager")
                            }
                        ) {
                            Text(text = R.string.jump_to.strings)
                        }
                    }
                }
            }
        }

        item {
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
                    label = "AppInfoTransition",
                    modifier = Modifier.defaultMinSize(minHeight = 200.dp)
                ) { isLoading ->
                    if (isLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .defaultMinSize(minHeight = 150.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            LoadingIndicator(
                                modifier = Modifier
                                    .size(64.dp)
                            )
                            Text(
                                text = R.string.loading.strings,
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

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        mainPart()
    }
}
