package io.github.skydynamic.maidataviewer.core.network

import io.github.skydynamic.maidataviewer.Application
import io.github.skydynamic.maidataviewer.core.config.AppConfig
import kotlinx.coroutines.flow.first
import java.net.URL

suspend fun ResourceNode.getUrl(point: String): URL {
    return if (this != ResourceNode.CUSTOM) {
        URL("$url/$point")
    } else {
        URL(AppConfig(Application.application).read {
            it[AppConfig.customNodeUrl] ?: ""
        }.first().apply {
            this.ifEmpty {
                ResourceNode.DIRECT.url
            }
        } + "/$point")
    }
}

enum class ResourceNode(
    val nodeName: String,
    val url: String
) {
    DIRECT("direct", "https://maimai-assets.skydynamic.top"),
    HK("hk", "https://maimai-assets-cdn-aliyun.skydynamic.top"),
    CUSTOM("custom", "");

    companion object {
        fun getNode(nodeName: String): ResourceNode {
            return entries.firstOrNull { it.nodeName == nodeName } ?: DIRECT
        }

        suspend fun getCurrentNode(): ResourceNode {
            return getNode(AppConfig(Application.application).read {
                it[AppConfig.chooseNode] ?: DIRECT.nodeName
            }.first())
        }
    }
}