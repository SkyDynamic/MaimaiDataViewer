package io.github.skydynamic.maidataviewer.ui.component.card

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.skydynamic.maidataviewer.core.noRippleClickable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CollapsibleSearchCard(
    modifier: Modifier = Modifier,
    isCollapsed: Boolean,
    onCollapseToggle: () -> Unit,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    ShadowElevatedCard(
        modifier = modifier
            .wrapContentHeight()
            .animateContentSize()
    ) {
        AnimatedContent(
            targetState = isCollapsed,
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                slideInVertically(
                    animationSpec = tween(durationMillis = 300),
                    initialOffsetY = { -it }
                ) + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
            },
        ) { targetState ->
            if (!targetState) {
                Column {
                    content()
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(horizontal = 4.dp)
                .noRippleClickable(onClick = onCollapseToggle),
            horizontalArrangement = Arrangement.Center
        ) {
            if (isCollapsed) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Collapsed")
            } else {
                Icon(Icons.Default.ArrowDropUp, contentDescription = "Expanded")
            }
        }
    }
}