package io.github.skydynamic.maidataviewer.ui.component.text

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TextTitleGroup(
    modifier: Modifier = Modifier,
    title: String,
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    titleWeight: FontWeight = FontWeight.Bold,
    titleColor: Color = Color.Unspecified,
    titleAlignment: Alignment.Horizontal = Alignment.Start,
    subtitle: String = "",
    subtitleStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    subtitleWeight: FontWeight = FontWeight.Normal,
    subtitleColor: Color = Color.Unspecified,
    subtitleAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            modifier = Modifier
                .align(titleAlignment),
            style = titleStyle,
            fontWeight = titleWeight,
            color = titleColor
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                modifier = Modifier
                    .align(subtitleAlignment),
                style = subtitleStyle,
                fontWeight = subtitleWeight,
                color = subtitleColor
            )
        }
    }
}

@Composable
fun AnimatedTextTitleGroup(
    modifier: Modifier = Modifier,
    title: String,
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    titleWeight: FontWeight = FontWeight.Bold,
    titleColor: Color = Color.Unspecified,
    titleAlignment: Alignment.Horizontal = Alignment.Start,
    subtitle: String = "",
    subtitleStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    subtitleWeight: FontWeight = FontWeight.Normal,
    subtitleColor: Color = Color.Unspecified,
    subtitleAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = title,
            modifier = Modifier
                .align(titleAlignment),
            style = titleStyle,
            fontWeight = titleWeight,
            color = titleColor
        )

        AnimatedContent(
            targetState = subtitle,
            transitionSpec = {
                slideInHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } +
                        fadeIn(animationSpec = tween(300)) togetherWith
                        slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth } +
                        fadeOut(animationSpec = tween(300))
            },
            label = "SubtitleAnimation"
        ) { targetSubtitle ->
            Text(
                text = targetSubtitle,
                modifier = Modifier
                    .align(subtitleAlignment),
                style = subtitleStyle,
                fontWeight = subtitleWeight,
                color = subtitleColor
            )
        }
    }
}
