package io.github.skydynamic.maidataviewer.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class DataTableRowTextValue {
    data class Text(val text: String) : DataTableRowTextValue()
    data class TextComposable(val content: @Composable () -> Unit) : DataTableRowTextValue()
}

sealed class DataTableRowStyleTextColor {
    data class TextColor(val color: Color?) : DataTableRowStyleTextColor()
    data class TextColorComposable(val content: @Composable () -> Color) :
        DataTableRowStyleTextColor()
}

data class DataTableRowStyle(
    val textColor: DataTableRowStyleTextColor,
    val backgroundColor: Color? = null,
    val textAlign: TextAlign? = null,
    val fontWeight: FontWeight? = null,
    val fontStyle: FontStyle? = null
) {
    constructor(
        textColor: @Composable () -> Color,
        backgroundColor: Color? = null,
        textAlign: TextAlign? = null,
        fontWeight: FontWeight? = null,
        fontStyle: FontStyle? = null
    ) : this(
        textColor = DataTableRowStyleTextColor.TextColorComposable(textColor),
        backgroundColor = backgroundColor,
        textAlign = textAlign,
        fontWeight = fontWeight,
        fontStyle = fontStyle
    )
}

fun DataTableRowStyle(
    textColor: Color? = null,
    backgroundColor: Color? = null,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null
): DataTableRowStyle = DataTableRowStyle(
    textColor = DataTableRowStyleTextColor.TextColor(textColor),
    backgroundColor = backgroundColor,
    textAlign = textAlign,
    fontWeight = fontWeight,
    fontStyle = fontStyle
)

data class DataTableColumn(
    val name: String,
    val width: Int = 100,
    val style: DataTableRowStyle = DataTableRowStyle()
)

data class DataTableRowValue(
    val value: DataTableRowTextValue,
    val style: DataTableRowStyle = DataTableRowStyle()
)

fun DataTableRowValue(
    value: String,
    style: DataTableRowStyle = DataTableRowStyle()
) = DataTableRowValue(DataTableRowTextValue.Text(value), style)

fun DataTableRowValue(
    value: @Composable () -> Unit,
    style: DataTableRowStyle = DataTableRowStyle()
) = DataTableRowValue(DataTableRowTextValue.TextComposable(value), style)

data class BasicDataTableRow(
    val values: List<DataTableRowValue>,
    val height: Int = 40
)

@Composable
fun DataTable(
    columns: List<DataTableColumn>,
    rows: List<BasicDataTableRow>,
    modifier: Modifier = Modifier,
    showVerticalDivider: Boolean = true,
    showHorizontalDivider: Boolean = true,
    enableHorizontalScroll: Boolean = true,
    enableFixColumnWidth: Boolean = false
) {
    val tableContent: @Composable (overrideColumns: List<DataTableColumn>?) -> Unit =
        @Composable { overrideColumns ->
            val columns = overrideColumns ?: columns

            val horizontalDividerWidth = (columns.sumOf { it.width } + (columns.size - 1)).dp
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    columns.forEachIndexed { index, column ->
                        Box(
                            modifier = Modifier
                                .width(column.width.dp)
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp),
                            contentAlignment = when (column.style.textAlign) {
                                TextAlign.Center -> Alignment.Center
                                TextAlign.End -> Alignment.CenterEnd
                                else -> Alignment.CenterStart
                            }
                        ) {
                            val textColor = when (column.style.textColor) {
                                is DataTableRowStyleTextColor.TextColor -> column.style.textColor.color
                                is DataTableRowStyleTextColor.TextColorComposable -> column.style.textColor.content()
                            }
                            Text(
                                text = column.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = column.style.fontWeight ?: FontWeight.Bold,
                                color = textColor ?: MaterialTheme.colorScheme.onSurface,
                                fontStyle = column.style.fontStyle ?: FontStyle.Normal,
                                autoSize = TextAutoSize.StepBased(minFontSize = 6.sp, maxFontSize = 14.sp)
                            )
                        }

                        if (showVerticalDivider && index < columns.size - 1) {
                            VerticalDivider(
                                modifier = Modifier
                                    .fillMaxHeight(),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                if (showHorizontalDivider) {
                    HorizontalDivider(
                        modifier = Modifier
                            .width(horizontalDividerWidth),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(rows) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(row.height.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            row.values.forEachIndexed { index, cell ->
                                if (index < columns.size) {
                                    val column = columns[index]
                                    Box(
                                        modifier = Modifier
                                            .width(column.width.dp)
                                            .fillMaxHeight()
                                            .background(
                                                cell.style.backgroundColor ?: Color.Transparent
                                            )
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = when (cell.style.textAlign
                                            ?: column.style.textAlign) {
                                            TextAlign.Center -> Alignment.Center
                                            TextAlign.End -> Alignment.CenterEnd
                                            else -> Alignment.CenterStart
                                        }
                                    ) {
                                        val textColor = when (cell.style.textColor) {
                                            is DataTableRowStyleTextColor.TextColor -> cell.style.textColor.color
                                            is DataTableRowStyleTextColor.TextColorComposable -> cell.style.textColor.content()
                                        }
                                        when (cell.value) {
                                            is DataTableRowTextValue.Text -> Text(
                                                text = cell.value.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = textColor
                                                    ?: MaterialTheme.colorScheme.onSurface,
                                                fontWeight = cell.style.fontWeight
                                                    ?: column.style.fontWeight ?: FontWeight.Normal,
                                                fontStyle = cell.style.fontStyle
                                                    ?: column.style.fontStyle ?: FontStyle.Normal,
                                                fontSize = when {
                                                    cell.style.fontWeight == FontWeight.Bold -> 14.sp
                                                    column.style.fontWeight == FontWeight.Bold -> 14.sp
                                                    else -> 12.sp
                                                },
                                                autoSize = TextAutoSize.StepBased(minFontSize = 6.sp, maxFontSize = 14.sp)
                                            )

                                            is DataTableRowTextValue.TextComposable -> cell.value.content()
                                        }
                                    }

                                    if (showVerticalDivider && index < columns.size - 1) {
                                        VerticalDivider(
                                            modifier = Modifier
                                                .fillMaxHeight(),
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }

                        if (showHorizontalDivider && rows.indexOf(row) < rows.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .width(horizontalDividerWidth),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

    if (enableHorizontalScroll && !enableFixColumnWidth) {
        Box(
            modifier = modifier
                .horizontalScroll(rememberScrollState())
        ) {
            tableContent(null)
        }
    } else if (enableFixColumnWidth) {
        BoxWithConstraints(
            modifier = modifier
        ) {
            val maxWidth = constraints.maxWidth

            val tableWidth = columns.sumOf { it.width }
            val freeWidth = maxWidth - tableWidth
            val avgFreeWidth = freeWidth / (columns.size * 8)

            val fixColumns = if (maxWidth > tableWidth) {
                columns.map {
                    it.copy(width = it.width + avgFreeWidth)
                }
            } else {
                columns
            }

            val finalWidth = fixColumns.sumOf { it.width }

            Box(
                modifier = Modifier
                    .width(finalWidth.dp)
                    .fillMaxHeight()
                    .align(Alignment.Center)
            ) {
                tableContent(fixColumns)
            }
        }
    } else {
        tableContent(null)
    }
}

