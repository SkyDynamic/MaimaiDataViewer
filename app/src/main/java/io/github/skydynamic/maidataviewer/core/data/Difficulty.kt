package io.github.skydynamic.maidataviewer.core.data

import androidx.compose.ui.graphics.Color

enum class Difficulty(val diffName: String, val color: Color) {
    BASIC("Basic", Color(28, 133, 0)),
    ADVANCED("Advanced", Color(168, 137, 0)),
    EXPERT("Expert", Color(220, 40, 40)),
    MASTER("Master", Color(165, 0, 235)),
    REMASTER("Re:Mas", Color(186, 153, 255))
}