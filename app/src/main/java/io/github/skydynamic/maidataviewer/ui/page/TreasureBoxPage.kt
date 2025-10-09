package io.github.skydynamic.maidataviewer.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.getString
import io.github.skydynamic.maidataviewer.ui.component.card.ShadowElevatedCard
import io.github.skydynamic.maidataviewer.ui.component.dialog.RatingCalculatorDialog


enum class DataTool(
    val toolName: String,
    val desc: String,
    val icon: Int,
    val action: (MutableState<Boolean>) -> Unit
) {
    RATING_CALCULATOR(
        R.string.rating_calculator.getString(),
        R.string.rating_calculator_desc.getString(),
        R.drawable.target,
        {
            it.value = true
        }
    ),
}

@Composable
fun TreasureBoxPage() {
    val showRatingCalculatorDialog = remember {
        mutableStateOf(false)
    }

    when {
        showRatingCalculatorDialog.value -> {
            RatingCalculatorDialog(
                onDismiss = {
                    showRatingCalculatorDialog.value = false
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
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
                    .padding(16.dp)
            ) {
                Text(
                    text = R.string.data_tool.getString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = R.string.data_tool_desc.getString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    DataTool.entries.forEach {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .padding(8.dp)
                                .clickable { it.action(showRatingCalculatorDialog) }
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = MaterialTheme.shapes.medium
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Image(
                                painter = painterResource(it.icon),
                                contentDescription = null,
                            )

                            Text(
                                text = it.toolName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = it.desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                            )
                        }
                    }
                }
            }
        }
    }
}