package io.github.skydynamic.maidataviewer.core.manager.collection

import android.content.res.AssetManager
import android.util.Log
import io.github.skydynamic.maidataviewer.core.MaiVersion
import io.github.skydynamic.maidataviewer.core.data.MaimaiCommonCollectionData
import io.github.skydynamic.maidataviewer.core.manager.resource.MaimaiResourceManager
import io.github.skydynamic.maidataviewer.core.mkdirsIfNotExists
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

class MaimaiIconManager(
    override val assetManager: AssetManager,
    override val resPath: File,
    override val httpClient: AppHttpClient
) : MaimaiResourceManager, CollectionManager<MaimaiCommonCollectionData> {
    override var _isLoaded: Boolean = false
    override var _currentCollectionVersion: MaiVersion = MaiVersion(-1, 0)

    val assetsUrl = "https://maimai-assets.skydynamic.top/icon"

    private var iconData: Map<Int, MaimaiCommonCollectionData> = emptyMap()

    override val collectionType: CollectionType = CollectionType.ICON

    @Serializable
    data class IconData(
        val version: String,
        val data: List<MaimaiCommonCollectionData>
    )

    override fun loadCollectionData() {
        val file = resPath.resolve("icon_list.json")
        if (file.exists()) {
            val json = file.readText()
            val iconData = Json.Default.decodeFromString<IconData>(json)
            currentCollectionVersion = MaiVersion.Companion.tryParse(iconData.version)!!
            this.iconData = iconData.data.associateBy { it.id }
            isLoaded = true
        }
    }

    override fun getCollection(id: Int): MaimaiCommonCollectionData? {
        return iconData[id]
    }

    override fun search(
        keyword: String,
        filterAction: ((List<MaimaiCommonCollectionData>) -> List<MaimaiCommonCollectionData>)?
    ): List<MaimaiCommonCollectionData> {
        val result = iconData.values.filter {
            it.name?.contains(keyword) == true
            || it.normalText?.contains(keyword) == true
        }

        if (filterAction != null) {
            return filterAction(result)
        }

        return result
    }

    override suspend fun downloadCollectionData(onFinished: (MaiVersion?) -> Unit) {
        try {
            val iconData = httpClient.request {
                val response = it.get("${this.baseApiUrl}/update")
                response.body<IconData>()
            }

            val latestVersion = MaiVersion.Companion.tryParse(iconData!!.version)
            if (latestVersion != null && latestVersion > currentCollectionVersion) {
                currentCollectionVersion = latestVersion
                resPath.resolve("icon_list.json")
                    .writeText(Json.Default.encodeToString(iconData))
            }

            onFinished(latestVersion)
        } catch (e: Exception) {
            Log.e("IconDataManager", "Error downloading icon data", e)
        }
    }

    override suspend fun getResFile(id: Int): File? {
        val iconPath = resPath.resolve("icon")
        iconPath.mkdirsIfNotExists()

        val iconFile = iconPath.resolve("$id.png")
        if (!iconFile.exists()) {
            httpClient.request {
                val request = it.get("$assetsUrl/$id.png")
                if (request.status.value == 200) {
                    request.bodyAsChannel()
                        .copyTo(iconFile.outputStream())
                } else {
                    Log.e("MaimaiIconManager", "Error downloading icon $id")
                }
            }
            Log.i("MaimaiIconManager", "Downloaded icon $id")
        }
        return if (iconFile.exists()) {
            iconFile
        } else {
            null
        }
    }

    override fun getResByteFromAssets(id: Int): ByteArray? {
        if (id >= 0) {
            val iconByte = assetManager.open("icon/$id.png").readBytes()
            return iconByte
        } else {
            Log.e("MaimaiIconManager", "Invalid icon id: $id")
            return null
        }
    }

    companion object {
        private var instance: MaimaiIconManager? = null

        fun build(
            assetsManager: AssetManager,
            resPath: File,
            httpClient: AppHttpClient
        ): MaimaiIconManager {
            if (instance == null) {
                instance = MaimaiIconManager(assetsManager, resPath, httpClient)
            }
            if (!instance!!.isLoaded) {
                instance!!.loadCollectionData()
            }
            return instance!!
        }
    }
}