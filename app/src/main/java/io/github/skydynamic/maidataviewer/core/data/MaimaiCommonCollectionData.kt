package io.github.skydynamic.maidataviewer.core.data

import kotlinx.serialization.Serializable

@Serializable
data class MaimaiCommonCollectionData(
    override val id: Int,
    override val name: String?,
    override val normalText: String?,
    val genre: Int
) : IMaimaiCollectionData
