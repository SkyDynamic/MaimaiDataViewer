package io.github.skydynamic.maidataviewer.core.data

import kotlinx.serialization.Serializable

@Serializable
data class MaimaiAliasData(
    val id: Int,
    val aliases: List<String>,
)