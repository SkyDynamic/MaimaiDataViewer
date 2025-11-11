package io.github.skydynamic.maidataviewer.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.core.strings
import io.github.skydynamic.maidataviewer.core.manager.MaiGenreManager
import io.github.skydynamic.maidataviewer.core.manager.resource.ResourceManagerType
import io.github.skydynamic.maidataviewer.viewmodel.GlobalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MusicSimpleCard(
    modifier: Modifier = Modifier,
    music: MaimaiMusicData,
    onClick: () -> Unit
) {
    val defaultJacketFile = remember { ResourceManagerType.JACKET.instance!!.getResByteFromAssets(0) }
    var jacketFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(music.id) {
        GlobalViewModel.viewModelScope.launch(Dispatchers.IO) {
            jacketFile = try {
                ResourceManagerType.JACKET.instance!!.getResFile(music.id)
            } catch (_: Exception) {
                null
            }
        }
    }

    ShadowElevatedCard(
        modifier = modifier,
        clickable = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(jacketFile ?: defaultJacketFile)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .size(80.dp)
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = music.name ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(0.65f)
                            .fillMaxWidth(),
                        maxLines = 1
                    )

                    Box(
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Text(
                            text = "#${music.id}",
                            style = MaterialTheme.typography.bodySmallEmphasized,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.Center),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .height(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = music.artist ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )

                    val isDX = music.id in 10000 until 100000
                    val isStage = music.id >= 100000

                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isDX) {
                                    Color(0xFFF57C00)
                                } else if (isStage) {
                                    Color(0xFF7B1FA2)
                                } else {
                                    Color(0xFF0288D1)
                                }
                            )
                    ) {
                        Text(
                            text = if (isDX) {
                                R.string.dx.strings
                            } else if (isStage) {
                                R.string.stage.strings
                            } else {
                                R.string.standard.strings
                            },
                            style = MaterialTheme.typography.bodySmallEmphasized,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(start = 2.dp, end = 2.dp)
                                .align(Alignment.Center),
                            color = Color.White,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 8.sp,
                                maxFontSize = 12.sp
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 35.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.25f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x662196F3)),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = music.addVersion.name?.replace(
                                "plus",
                                "+",
                                true
                            ) ?: "",
                            color = Color(0xFF1976D2),
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 4.dp),
                            textAlign = TextAlign.Center,
                            autoSize = TextAutoSize
                                .StepBased(
                                    minFontSize = 7.sp,
                                    maxFontSize = 16.sp,
                                    stepSize = 0.1.sp
                                )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.25f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x668BC34A)),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = MaiGenreManager.musicGenre.getGenreName(music.genre),
                            color = Color(0xFF4CAF50),
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 4.dp),
                            textAlign = TextAlign.Center,
                            autoSize = TextAutoSize
                                .StepBased(
                                    minFontSize = 8.sp,
                                    maxFontSize = 12.sp,
                                    stepSize = 0.1.sp
                                )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.5f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x66FF9800)),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = music.difficulties.filter {
                                it.levelLabel != "0"
                            }.joinToString("/") {
                                it.levelLabel
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722),
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 4.dp),
                            textAlign = TextAlign.Center,
                            autoSize = TextAutoSize
                                .StepBased(
                                    minFontSize = 6.sp,
                                    maxFontSize = 12.sp,
                                    stepSize = 0.1.sp
                                )
                        )
                    }
                }
            }
        }
    }
}
