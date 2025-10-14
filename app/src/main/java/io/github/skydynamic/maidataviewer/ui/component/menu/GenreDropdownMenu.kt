package io.github.skydynamic.maidataviewer.ui.component.menu

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.getString
import io.github.skydynamic.maidataviewer.core.manager.GenreType
import io.github.skydynamic.maidataviewer.core.manager.MaiGenreManager

@Composable
fun GenreDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelectedChange: (Int) -> Unit,
    genreType: GenreType,
    excludeStage: Boolean = false
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .padding(4.dp)
            .heightIn(max = 400.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        DropdownMenuItem(
            text = { Text(R.string.all.getString()) },
            onClick = {
                onSelectedChange(-1)
                onDismissRequest()
            }
        )
        MaiGenreManager.get(genreType)
            .getGenreData()
            .run {
                if (excludeStage && genreType == GenreType.MUSIC) {
                    filter { it.id != 107 }
                } else {
                    this
                }
            }
            .forEach {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onSelectedChange(it.id)
                        onDismissRequest()
                    }
                )
            }
    }
}