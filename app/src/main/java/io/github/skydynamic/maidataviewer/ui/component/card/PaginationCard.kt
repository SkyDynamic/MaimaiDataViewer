package io.github.skydynamic.maidataviewer.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.skydynamic.maidataviewer.core.noRippleClickable

@Composable
fun PaginationCard(
    modifier: Modifier = Modifier,
    currentPage: Int,
    totalPage: Int,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.1f)
                .clip(shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .noRippleClickable(onClick = {
                    if (currentPage <= 1) return@noRippleClickable
                    onPageChange(currentPage - 1)
                }),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = if (currentPage <= 1)
                    MaterialTheme.colorScheme.outline
                else MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .weight(0.8f)
                .noRippleClickable(onClick = { }) // 防止点到下面的卡片
        ) {
            PageNumbers(
                currentPage,
                totalPage,
                onPageChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.1f)
                .clip(shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .noRippleClickable(onClick = {
                    if (currentPage >= totalPage) return@noRippleClickable
                    onPageChange(currentPage + 1)
                }),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ">",
                color = if (currentPage >= totalPage)
                    MaterialTheme.colorScheme.outline
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PageNumbers(
    currentPage: Int,
    totalPage: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagesToShow = generatePageList(currentPage, totalPage)

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalArrangement = Arrangement.Center,
        maxLines = 1
    ) {
        pagesToShow.forEach { page ->
            if (page == -1) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(30.dp)
                        .noRippleClickable { onPageChange(page) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "...",
                        modifier = Modifier
                            .padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(25.dp)
                        .noRippleClickable { onPageChange(page) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = page.toString(),
                        modifier = Modifier
                            .padding(horizontal = 4.dp),
                        color = if (page == currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (page == currentPage) FontWeight.Bold else FontWeight.Normal,
                        autoSize = TextAutoSize.StepBased(6.sp, 16.sp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}


fun generatePageList(currentPage: Int, totalPage: Int): List<Int> {
    if (totalPage <= 7) {
        return (1..totalPage).toList()
    }

    val pages = mutableListOf<Int>()

    when {
        currentPage <= 4 -> {
            pages.addAll(1..5)
            pages.add(-1)
            pages.add(totalPage)
        }
        currentPage >= totalPage - 3 -> {
            pages.add(1)
            pages.add(-1)
            pages.addAll(totalPage - 4..totalPage)
        }
        else -> {
            pages.add(1)
            pages.add(-1)
            pages.addAll(currentPage - 2..currentPage + 2)
            pages.add(-1)
            pages.add(totalPage)
        }
    }

    return pages
}