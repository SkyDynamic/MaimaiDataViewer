package io.github.skydynamic.maidataviewer.ui.page

import android.widget.Toast
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastJoinToString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.skydynamic.maidataviewer.Application.Companion.application
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.AchievementCalculator
import io.github.skydynamic.maidataviewer.core.ExtensionMethods.buildDataTableRow
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.core.getString
import io.github.skydynamic.maidataviewer.core.manager.MaiGenreManager
import io.github.skydynamic.maidataviewer.core.manager.MaimaiJacketManager.Companion.instance
import io.github.skydynamic.maidataviewer.core.manager.MusicAliasManager
import io.github.skydynamic.maidataviewer.ui.component.BasicDataTableRow
import io.github.skydynamic.maidataviewer.ui.component.DataTable
import io.github.skydynamic.maidataviewer.ui.component.DataTableColumn
import io.github.skydynamic.maidataviewer.ui.component.DataTableRowStyle
import io.github.skydynamic.maidataviewer.ui.component.WindowInsetsSpacer
import io.github.skydynamic.maidataviewer.ui.component.WindowInsetsSpacer.TopPaddingSpacer
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard
import io.github.skydynamic.maidataviewer.ui.component.dialog.TextDialog
import io.github.skydynamic.maidataviewer.viewmodel.GlobalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.absoluteValue

object TempState : ViewModel() {
    var showAliasDialog by mutableStateOf(false)

    fun reset() {
        showAliasDialog = false
    }
}

enum class Difficulty(val diffName: String, val color: Color) {
    BASIC("Basic", Color(28, 133, 0)),
    ADVANCED("Advanced", Color(168, 137, 0)),
    EXPERT("Expert", Color(220, 40, 40)),
    MASTER("Master", Color(165, 0, 235)),
    REMASTER("Re:Mas", Color(186, 153, 255))
}

enum class NoteType(val typeNmae: String) {
    TAP("Tap"),
    HOLD("Hold"),
    SLIDE("Slide"),
    TOUCH("Touch"),
    BREAK("Break")
}

val missTextColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) {
        Color(206, 212, 218)
    } else {
        Color(134, 142, 150)
    }

private val dataTableHeader: List<DataTableColumn> = listOf(
    DataTableColumn(
        "101-",
        width = 60,
        style = DataTableRowStyle(
            textAlign = TextAlign.Center
        )
    ),
    DataTableColumn(
        R.string.arch_table_count.getString(),
        width = 60,
        style = DataTableRowStyle(
            textAlign = TextAlign.Center
        )
    ),
    DataTableColumn(
        "PERFECT",
        style = DataTableRowStyle(
            textColor = Color(245, 124, 0, 255),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    ),
    DataTableColumn(
        "GREAT",
        style = DataTableRowStyle(
            textColor = Color(236, 64, 122, 255),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    ),
    DataTableColumn(
        "GOOD",
        style = DataTableRowStyle(
            textColor = Color(56, 142, 60, 255),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    ),
    DataTableColumn(
        "MISS",
        style = DataTableRowStyle(
            textColor = {
                missTextColor
            },
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    )
)

@Composable
fun InfoBox(
    type: String,
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .height(16.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )

        Text(
            text = "$type: ",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier
                .padding(start = 4.dp),
            autoSize = TextAutoSize
                .StepBased(minFontSize = 8.sp),
            color = color
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth(),
                autoSize = TextAutoSize
                    .StepBased(minFontSize = 8.sp),
                color = color
            )
        }
    }
}

fun getFaultTolerance(
    achievementData: AchievementCalculator.AchievementData,
    noteType: NoteType,
    faultValue: Float
): List<String> {
    return when (noteType) {
        NoteType.TAP -> {
            listOf(
                (faultValue / achievementData.tap.great.absoluteValue).toInt().toString(),
                (faultValue / achievementData.tap.good.absoluteValue).toInt().toString(),
                (faultValue / achievementData.tap.miss.absoluteValue).toInt().toString()
            )
        }
        NoteType.HOLD -> {
            listOf(
                (faultValue / achievementData.hold.great.absoluteValue).toInt().toString(),
                (faultValue / achievementData.hold.good.absoluteValue).toInt().toString(),
                (faultValue / achievementData.hold.miss.absoluteValue).toInt().toString()
            )
        }
        NoteType.SLIDE -> {
            listOf(
                (faultValue / achievementData.slide.great.absoluteValue).toInt().toString(),
                (faultValue / achievementData.slide.good.absoluteValue).toInt().toString(),
                (faultValue / achievementData.slide.miss.absoluteValue).toInt().toString()
            )
        }
        NoteType.TOUCH -> {
            if (achievementData.touch.total == 0) {
                return listOf("-", "-", "-")
            }
            listOf(
                (faultValue / achievementData.touch.great.absoluteValue).toInt().toString(),
                (faultValue / achievementData.touch.good.absoluteValue).toInt().toString(),
                (faultValue / achievementData.touch.miss.absoluteValue).toInt().toString()
            )
        }
        NoteType.BREAK -> {
            listOf(
                (faultValue / achievementData.breakNote.great.late.absoluteValue).toInt().toString(),
                (faultValue / achievementData.breakNote.good.absoluteValue).toInt().toString(),
                (faultValue / achievementData.breakNote.miss.absoluteValue).toInt().toString()
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MusicDetailPage(
    music: MaimaiMusicData,
    onBackPressed: () -> Unit,
    onDataTableClickable: (List<DataTableColumn>, List<BasicDataTableRow>) -> Unit
) {
    val isDX = music.id in 10000 until 100000

    var currentChoiceDifficulty by remember { mutableIntStateOf(0) }
    val defaultJacketFile = remember { instance.getJacketFromAssets(0) }
    var jacketFile by remember { mutableStateOf<File?>(null) }
    var currentChoiceNoteType by remember { mutableStateOf(NoteType.TAP) }

    val achievementDataList by remember(music.difficulties) {
        mutableStateOf(music.difficulties.map { difficulty ->
            val calculator = AchievementCalculator(difficulty.notes)
            calculator.getResult()
        })
    }

    LaunchedEffect(music.id) {
        GlobalViewModel.viewModelScope.launch(Dispatchers.IO) {
            jacketFile = try {
                instance.getJacketFile(music.id)
            } catch (_: Exception) {
                null
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            TempState.reset()
        }
    }

    val color = Difficulty.entries[currentChoiceDifficulty].color
    val commonRoundedShape = RoundedCornerShape(8.dp)

    when {
        TempState.showAliasDialog -> {
            TextDialog(
                text = R.string.song_exists_alias
                    .getString()
                    .format(
                        MusicAliasManager.getAlias(music.id)
                            .fastJoinToString("\n")
                    ),
                onDismiss = {
                    TempState.showAliasDialog = false
                }
            )
        }
    }

    Box {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .wrapContentHeight()
                .heightIn(max = 20000.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(Modifier.height(40.dp))
            TopPaddingSpacer()

            MusicInfoCard(
                music = music,
                jacketFile = jacketFile,
                defaultJacketFile = defaultJacketFile,
                color = color,
                commonRoundedShape = commonRoundedShape
            )

            DifficultySelectionRow(
                difficulties = music.difficulties,
                currentChoiceDifficulty = currentChoiceDifficulty,
                color = color,
                commonRoundedShape = commonRoundedShape,
                onDifficultySelected = { index -> currentChoiceDifficulty = index }
            )

            MusicDetailCard(
                music = music,
                currentChoiceDifficulty = currentChoiceDifficulty,
                isDX = isDX,
                color = color,
                achievementDataList = achievementDataList,
                dataTableHeader = dataTableHeader,
                onDataTableClickable = onDataTableClickable
            )

            FaultToleranceCard(
                achievementDataList = achievementDataList,
                currentChoiceDifficulty = currentChoiceDifficulty,
                currentChoiceNoteType = currentChoiceNoteType,
                onNoteTypeSelected = { noteType -> currentChoiceNoteType = noteType }
            )

            ActionButtonsRow(
                jacketFile = jacketFile,
                musicName = music.name ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    )
            )

            Spacer(modifier = Modifier.height(15.dp))
        }

        TopAppBar(
            onBackPressed = onBackPressed,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsetsSpacer.topPadding,
                    bottom = WindowInsetsSpacer.bottomPadding
                )
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MusicInfoCard(
    music: MaimaiMusicData,
    jacketFile: File?,
    defaultJacketFile: ByteArray?,
    color: Color,
    commonRoundedShape: RoundedCornerShape
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
            .background(
                color = color.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                color = color,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(jacketFile ?: defaultJacketFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(commonRoundedShape)
                        .size(140.dp)
                        .border(
                            width = 2.dp,
                            color = color,
                            shape = commonRoundedShape
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = music.name ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp),
                                textAlign = TextAlign.Start,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = 8.dp)
                            .background(
                                Color.White,
                                shape = commonRoundedShape
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 8.dp
                                ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoBox(
                                type = R.string.music_type.getString(),
                                text = MaiGenreManager.musicGenre.getGenreName(music.genre),
                                color = color
                            )
                            InfoBox(
                                type = "BPM",
                                text = music.bpm.toString(),
                                color = color
                            )
                            InfoBox(
                                type = R.string.music_version.getString(),
                                text = MaiGenreManager.versionGenre.getGenreName(music.addVersion.id),
                                color = color
                            )
                            InfoBox(
                                type = R.string.artist.getString(),
                                text = music.artist ?: "",
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DifficultySelectionRow(
    difficulties: List<MaimaiMusicData.MaimaiMusicDifficultyData>,
    currentChoiceDifficulty: Int,
    color: Color,
    commonRoundedShape: RoundedCornerShape,
    onDifficultySelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        difficulties.forEachIndexed { index, difficulty ->
            val isSelected = currentChoiceDifficulty == index

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .weight(1f / difficulties.size)
                        .height(60.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    color,
                                    color.copy(alpha = 0.9f),
                                    color.copy(alpha = 0.6f),
                                ),
                                start = Offset.Zero,
                                end = Offset.Infinite
                            ),
                            shape = commonRoundedShape
                        )
                        .border(
                            width = 2.dp,
                            color = color,
                            shape = commonRoundedShape
                        )
                        .padding(horizontal = 4.dp)
                ) {
                    DifficultyContent(index, difficulty, Color.White)
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f / difficulties.size)
                        .height(60.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = commonRoundedShape
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = commonRoundedShape
                        )
                        .padding(horizontal = 4.dp)
                        .clickable {
                            onDifficultySelected(index)
                        }
                ) {
                    DifficultyContent(index, difficulty, MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
private fun MusicDetailCard(
    music: MaimaiMusicData,
    currentChoiceDifficulty: Int,
    isDX: Boolean,
    color: Color,
    achievementDataList: List<AchievementCalculator.AchievementData>,
    dataTableHeader: List<DataTableColumn>,
    onDataTableClickable: (List<DataTableColumn>, List<BasicDataTableRow>) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
            .background(
                color = color.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                color = color,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            InfoBox(
                type = R.string.music_level.getString(),
                text = "%.1f".format(music.difficulties[currentChoiceDifficulty].level),
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            InfoBox(
                type = R.string.designer.getString(),
                text = music.difficulties[currentChoiceDifficulty].noteDesigner,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            InfoBox(
                type = R.string.note_type.getString(),
                text = if (isDX) R.string.dx.getString() else R.string.standard.getString(),
                color = Color.White
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.White
            )

            val achievementData = achievementDataList[currentChoiceDifficulty]
                .buildDataTableRow()

            Row(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                )

                Text(
                    text = R.string.note_score_detail.getString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    textAlign = TextAlign.Start,
                    color = Color.White
                )
            }

            Text(
                text = "(${R.string.achievement_datatable_tips.getString()})",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 8.sp,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                textAlign = TextAlign.Start,
                color = Color.White
            )

            DataTable(
                columns = dataTableHeader,
                rows = achievementData,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        onDataTableClickable(dataTableHeader, achievementData)
                    }
            )
        }
    }
}

@Composable
private fun FaultToleranceCard(
    achievementDataList: List<AchievementCalculator.AchievementData>,
    currentChoiceDifficulty: Int,
    currentChoiceNoteType: NoteType,
    onNoteTypeSelected: (NoteType) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = R.string.fault_tolerance_calc.getString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 8.dp),
                maxItemsInEachRow = 3,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NoteType.entries.forEach { noteType ->
                    val isSelected = currentChoiceNoteType == noteType

                    val textColor = if (isSelected) {
                        Color.White
                    } else {
                        Color(0xFF0A305F)
                    }

                    val backgroundColor = if (isSelected) {
                        Color(0xFF284777)
                    } else {
                        Color(0xFFAAC7FF)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(backgroundColor)
                            .clickable {
                                onNoteTypeSelected(noteType)
                            }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = noteType.typeNmae,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            color = textColor,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            ShadowElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = R.string.fault_torlerance.getString()
                            .format(currentChoiceNoteType.typeNmae),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    FaultToleranceDetailCard(
                        achievementDataList = achievementDataList,
                        currentChoiceDifficulty = currentChoiceDifficulty,
                        currentChoiceNoteType = currentChoiceNoteType,
                        faultValue = 0.5f,
                        title = R.string.rate_fault_torlerance.getString()
                            .format("SSS+", "0.5%")
                    )

                    FaultToleranceDetailCard(
                        achievementDataList = achievementDataList,
                        currentChoiceDifficulty = currentChoiceDifficulty,
                        currentChoiceNoteType = currentChoiceNoteType,
                        faultValue = 1f,
                        title = R.string.rate_fault_torlerance.getString()
                            .format("SSS", "1%")
                    )
                }
            }
        }
    }
}

@Composable
private fun FaultToleranceDetailCard(
    achievementDataList: List<AchievementCalculator.AchievementData>,
    currentChoiceDifficulty: Int,
    currentChoiceNoteType: NoteType,
    faultValue: Float,
    title: String
) {
    ShadowElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                maxItemsInEachRow = 3,
                maxLines = 1,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.Center
            ) {
                val faultToleranceCount = getFaultTolerance(
                    achievementDataList[currentChoiceDifficulty],
                    currentChoiceNoteType,
                    faultValue
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Great:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )

                    Text(
                        text = faultToleranceCount[0],
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Good:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )

                    Text(
                        text = faultToleranceCount[1],
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Miss:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )

                    Text(
                        text = faultToleranceCount[2],
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(
    jacketFile: File?,
    musicName: String,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                if (jacketFile == null) {
                    Toast.makeText(
                        application,
                        R.string.save_jacket_failed.getString(),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    application.saveImageToGallery(
                        jacketFile.readBytes(),
                        R.string.save_jacket_success.getString()
                    )
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(30.dp),
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = R.string.save_jacket.getString(),
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 16.sp)
            )
        }

        Button(
            onClick = {
                application.openBilibiliSearch(musicName)
            },
            modifier = Modifier
                .weight(1f)
                .height(30.dp),
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = R.string.search_bilibili.getString(),
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 16.sp)
            )
        }

        Button(
            onClick = {
                TempState.showAliasDialog = true
            },
            modifier = Modifier
                .weight(1f)
                .height(30.dp),
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = R.string.view_music_alias.getString(),
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 16.sp)
            )
        }
    }
}

@Composable
private fun TopAppBar(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(
                    horizontal = 16.dp,
                    vertical = 4.dp
                ),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackPressed
            ) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = R.string.back.getString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DifficultyContent(
    index: Int,
    difficulty: MaimaiMusicData.MaimaiMusicDifficultyData,
    textColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = Difficulty.entries[index].diffName,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth(),
            fontSize = 11.5.sp,
            color = textColor,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Lv.${difficulty.levelLabel}",
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth(),
            fontSize = 10.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
