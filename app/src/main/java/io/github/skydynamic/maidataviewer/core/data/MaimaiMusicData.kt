package io.github.skydynamic.maidataviewer.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaimaiMusicData(
    val id: Int,
    val name: String? = "",
    val artist: String? = "",
    val bpm: Int,
    val difficulties: List<MaimaiMusicDifficultyData>,
    val genre: Int,
    val version: Int,
    val addVersion: MaimaiMusicAddVersionData
) {
    @Serializable
    data class MaimaiMusicDifficultyData(
        val level: Float,
        val levelLabel: String,
        val notes: MaimaiMusicDifficultyNotesData,
        val noteDesigner: String
    )

    @Serializable
    data class MaimaiMusicDifficultyNotesData(
        val tap: Int,
        val hold: Int,
        val slide: Int,
        val touch: Int,
        @SerialName("break") val breakNote: Int,
        val total: Int
    )

    @Serializable
    data class MaimaiMusicAddVersionData(
        val id: Int,
        val name: String? = "",
    )
}
