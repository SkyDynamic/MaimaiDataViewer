package io.github.skydynamic.maidataviewer.ui.page.treasurebox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.getString
import io.github.skydynamic.maidataviewer.core.utils.calcRating
import io.github.skydynamic.maidataviewer.core.utils.getMultiplierFactor
import io.github.skydynamic.maidataviewer.core.utils.getRank
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard

data class CalcResultStruct(
    val title: List<String> = listOf("Rank", "Rating", R.string.multiplierFactor.getString()),
    val value: List<String> = listOf("-", "-", "-")
) {
    companion object {
        val DEFAULT = CalcResultStruct()
    }
}

@Composable
fun RatingCalculatorPage(
    onBackPressed: () -> Unit
) {
    var musicLevel by remember { mutableStateOf("") }
    var achievement by remember { mutableStateOf("") }

    var musicLevelFocused by remember { mutableStateOf(false) }
    var achievementFocused by remember { mutableStateOf(false) }

    var calcResult by remember { mutableStateOf(CalcResultStruct.DEFAULT) }

    fun calc() {
        if (musicLevel.isEmpty() || achievement.isEmpty()) {
            calcResult = CalcResultStruct()
            return
        }
        calcResult = CalcResultStruct(
            value = listOf(
                getRank(achievement.toDouble()),
                calcRating(musicLevel.toDouble(), achievement.toDouble()).toString(),
                getMultiplierFactor(achievement.toDouble()).toString()
            )
        )
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
                    )
                }

                Text(
                    text = R.string.rating_calculator.getString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = R.string.rating_calculator_music_level.getString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    OutlinedTextField(
                        value = musicLevel,
                        onValueChange = {
                            if (it.toFloatOrNull() != null
                                && it.toFloat() <= 99f
                                && it.toFloat() >= 0f
                                || it.isEmpty()
                            ) {
                                musicLevel = it
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused && musicLevelFocused) {
                                    calc()
                                }
                                musicLevelFocused = focusState.isFocused
                            },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                calc()
                            }
                        ),
                        placeholder = {
                            Text(
                                text = R.string.rating_calculator_music_level_placeholder.getString(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    )

                    Text(
                        text = R.string.rating_calculator_achievement.getString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    OutlinedTextField(
                        value = achievement,
                        onValueChange = {
                            if (it.toFloatOrNull() != null
                                && it.toFloat() <= 101f
                                && it.toFloat() >= 0f
                                || it.isEmpty()
                            ) {
                                achievement = it
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused && achievementFocused) {
                                    calc()
                                }
                                achievementFocused = focusState.isFocused
                            },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                calc()
                            }
                        ),
                        placeholder = {
                            Text(
                                text = R.string.rating_calculator_achievement_placeholder.getString(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        suffix = {
                            Text(
                                text = "%",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    )

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        onClick = {
                            calc()
                        }
                    ) {
                        Text(
                            text = R.string.confirm.getString(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    FlowRow(
                        modifier = Modifier
                            .height(80.dp)
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.shapes.medium
                            ),
                        maxLines = 1,
                        maxItemsInEachRow = 3,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        calcResult.title.forEachIndexed { index, title ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Text(
                                    text = calcResult.value[index],
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}