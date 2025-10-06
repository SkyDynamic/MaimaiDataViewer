package io.github.skydynamic.maidataviewer.core

import androidx.compose.runtime.MutableState
import io.github.skydynamic.maidataviewer.Application.Companion.application
import kotlin.math.absoluteValue

fun Int.getString() = application.getString(this)
operator fun MutableState<Boolean>.not() = !value

fun Double.toAchievementString(): String {
    return when {
        this == 0.0 -> "0.0000%"
        this < 0 -> "-${"%.4f".format(this.absoluteValue)}%"
        else -> "${"%.4f".format(this)}%"
    }
}