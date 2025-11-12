package io.github.skydynamic.maidataviewer.core

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import io.github.skydynamic.maidataviewer.Application.Companion.application
import io.github.skydynamic.maidataviewer.core.manager.resource.MaimaiResourceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.File
import kotlin.math.absoluteValue

fun Int.getString() = application.getString(this)

@Stable
inline val Int.strings
    get() = getString()

fun Int.getFont() = application.getFont(this)

fun MaimaiResourceManager.getResFileAsync(id: Int, onFinished: (File?) -> Unit) {
    CoroutineScope(Dispatchers.IO).async {
        try {
            val file = getResFile(id)
            onFinished(file)
        } catch (e: Exception) {
            Log.e("MaimaiResourceManager", "getResFileAsync", e)
            onFinished(null)
        }
    }
}

fun MaimaiResourceManager.getResFileAsync(id: Int): Deferred<File?> {
    return CoroutineScope(Dispatchers.IO).async {
        try {
            getResFile(id)
        } catch (e: Exception) {
            Log.e("MaimaiResourceManager", "getResFileAsync", e)
            null
        }
    }
}

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

fun String.toFullWidth(): String {
    val stringBuilder = StringBuilder()
    for (c in this.toCharArray()) {
        if (c.code in 33..126) {
            stringBuilder.append((c.code + 65248).toChar())
        } else {
            stringBuilder.append(c)
        }
    }
    return stringBuilder.toString()
}

fun String.toHalfWidth(): String {
    val stringBuilder = StringBuilder()
    for (c in this.toCharArray()) {
        if (c.code in 65248..65374) {
            stringBuilder.append((c.code - 65248).toChar())
        } else {
            stringBuilder.append(c)
        }
    }
    return stringBuilder.toString()
}