package io.github.skydynamic.maidataviewer.ui.page.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Composable
fun BaseTextItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String = "",
) {
    Column(
        modifier = modifier
    ) {
        Text(text = title, fontSize = 18.sp)
        if (description.isNotEmpty()) {
            Text(
                text = description,
                fontSize = 12.sp,
                style = TextStyle.Default,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}