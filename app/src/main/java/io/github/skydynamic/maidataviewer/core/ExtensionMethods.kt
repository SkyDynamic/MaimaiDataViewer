package io.github.skydynamic.maidataviewer.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import io.github.skydynamic.maidataviewer.Application.Companion.application
import java.io.File
import kotlin.math.absoluteValue

fun Int.getString() = application.getString(this)
operator fun MutableState<Boolean>.not() = !value

fun File.mkdirsIfNotExists() {
    if (!this.exists()) {
        mkdirs()
    }
}

fun Double.toAchievementString(): String {
    return when {
        this == 0.0 -> "0.0000%"
        this < 0 -> "-${"%.4f".format(this.absoluteValue)}%"
        else -> "${"%.4f".format(this)}%"
    }
}

inline fun Modifier.noRippleClickable(crossinline onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}