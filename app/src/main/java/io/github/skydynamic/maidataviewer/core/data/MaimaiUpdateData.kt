package io.github.skydynamic.maidataviewer.core.data

import kotlinx.serialization.Serializable


@Serializable
data class MaimaiUpdateData(
    val version: String,
    val data: List<MaimaiMusicData>,
    val appendUpgrade: Boolean,
    val requiredVersion: List<String>
)