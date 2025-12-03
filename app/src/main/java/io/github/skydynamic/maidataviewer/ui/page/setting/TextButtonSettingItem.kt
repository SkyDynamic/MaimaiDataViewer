package io.github.skydynamic.maidataviewer.ui.page.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun TextButtonSettingItem(
    title: String,
    description: String,
    selectedValueStr: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(16.dp)
    ) {
        BaseTextItem(
            modifier = Modifier.fillMaxWidth(0.7f),
            title = title,
            description = description
        )

        Spacer(modifier = Modifier.weight(1.0f))

        TextButton(
            onClick = onClick
        ) {
            Text(
                text = selectedValueStr.lowercase().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}