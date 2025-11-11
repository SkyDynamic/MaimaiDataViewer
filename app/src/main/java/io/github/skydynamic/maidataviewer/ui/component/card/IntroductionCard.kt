package io.github.skydynamic.maidataviewer.ui.component.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.skydynamic.maidataviewer.ui.component.text.TextTitleGroup

@Composable
fun IntroductionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String = "",
    content: @Composable () -> Unit
) {
    ShadowElevatedCard(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TextTitleGroup(
                modifier = Modifier
                    .height(50.dp),
                title = title,
                subtitle = subtitle,
                subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))

            Box(
                modifier = Modifier
                    .heightIn(min = 80.dp)
                    .fillMaxSize()
            ) {
                content()
            }
        }
    }
}