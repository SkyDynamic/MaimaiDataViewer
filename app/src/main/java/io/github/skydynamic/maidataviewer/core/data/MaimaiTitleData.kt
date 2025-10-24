package io.github.skydynamic.maidataviewer.core.data

import io.github.skydynamic.maidataviewer.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaimaiTitleData(
    override val id: Int,
    override val name: String? = "",
    override val normalText: String? = "",
    val rareType: RareType,
) : IMaimaiCollectionData {
    enum class RareType(
        val resId: Int,
    ) {
        @SerialName("Normal")
        NORMAL(R.drawable.ic_maimai_shougou_normal),
        @SerialName("Bronze")
        BRONZE(R.drawable.ic_maimai_shougou_bronze),
        @SerialName("Silver")
        SILVER(R.drawable.ic_maimai_shougou_silver),
        @SerialName("Gold")
        GOLD(R.drawable.ic_maimai_shougou_gold),
        @SerialName("Rainbow")
        RAINBOW(R.drawable.ic_maimai_shougou_rainbow)
    }
}
