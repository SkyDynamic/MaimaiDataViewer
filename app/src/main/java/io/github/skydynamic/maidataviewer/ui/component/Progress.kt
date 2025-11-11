package io.github.skydynamic.maidataviewer.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnknownProgressCircularProgress(
    modifier: Modifier = Modifier,
    message: String = R.string.loading.strings,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    gapSize: Dp = ProgressIndicatorDefaults.CircularIndicatorTrackGapSize,
    color: Color = ProgressIndicatorDefaults.circularColor,
    trackColor: Color = ProgressIndicatorDefaults.circularIndeterminateTrackColor
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            strokeWidth = strokeWidth,
            gapSize = gapSize,
            color = color,
            trackColor = trackColor,
        )
        Text(
            text = message,
            modifier = Modifier.padding(16.dp)
        )
    }
}