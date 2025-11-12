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

class MaimaiFrameManager(
    override val assetManager: AssetManager,
    override val resPath: File,
    override val httpClient: AppHttpClient
) : MaimaiResourceManager, CollectionManager<MaimaiCommonCollectionData> {
    override var _isLoaded: Boolean = false
    override var _currentCollectionVersion: MaiVersion = MaiVersion(-1, 0)

    val assetsUrl = "https://maimai-assets.skydynamic.top/frame"

    private var frameData: Map<Int, MaimaiCommonCollectionData> = emptyMap()

    override val collectionType: CollectionType = CollectionType.FRAME

    @Serializable
    data class FrameData(
        val version: String,
        val data: List<MaimaiCommonCollectionData>
    )

    override fun loadCollectionData() {
        val file = resPath.resolve("frame_list.json")
        if (file.exists()) {
            val json = file.readText()
            val frameData = Json.decodeFromString<FrameData>(json)
            currentCollectionVersion = MaiVersion.tryParse(frameData.version)!!
            this.frameData = frameData.data.associateBy { it.id }
            isLoaded = true
        }
    }

    override fun getCollection(id: Int): MaimaiCommonCollectionData? {
        return frameData[id]
    }

    override fun search(
        keyword: String,
        filterAction: ((List<MaimaiCommonCollectionData>) -> List<MaimaiCommonCollectionData>)?
    ): List<MaimaiCommonCollectionData> {
        val result = frameData.values.filter {
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
            val frameData = httpClient.request {
                val response = it.get("${this.baseApiUrl}/update")
                response.body<FrameData>()
            }

            val latestVersion = MaiVersion.Companion.tryParse(frameData!!.version)
            if (latestVersion != null && latestVersion > currentCollectionVersion) {
                currentCollectionVersion = latestVersion
                resPath.resolve("frame_list.json")
                    .writeText(Json.Default.encodeToString(frameData))
            }

            onFinished(latestVersion)
        } catch (e: Exception) {
            Log.e("FrameDataManager", "Error downloading frame data", e)
        }
    }

    override suspend fun getResFile(id: Int): File? {
        val framePath = resPath.resolve("frame")
        framePath.mkdirsIfNotExists()

        val frameFile = framePath.resolve("$id.png")
        if (!frameFile.exists()) {
            httpClient.request {
                val request = it.get("$assetsUrl/$id.png")
                if (request.status.value == 200) {
                    request.bodyAsChannel()
                        .copyTo(frameFile.outputStream())
                } else {
                    Log.e("MaimaiFrameManager", "Error downloading frame $id")
                }
            }
            Log.i("MaimaiFrameManager", "Downloaded frame $id")
        }
        return if (frameFile.exists()) {
            frameFile
        } else {
            null
        }
    }

    override fun getResByteFromAssets(id: Int): ByteArray? {
        if (id >= 0) {
            val frameByte = assetManager.open("frame/$id.png").readBytes()
            return frameByte
        } else {
            Log.e("MaimaiFrameManager", "Invalid frame id: $id")
            return null
        }
    }

    companion object {
        private var instance: MaimaiFrameManager? = null

        fun build(
            assetsManager: AssetManager,
            resPath: File,
            httpClient: AppHttpClient
        ): MaimaiFrameManager {
            if (instance == null) {
                instance = MaimaiFrameManager(assetsManager, resPath, httpClient)
            }
            if (!instance!!.isLoaded) {
                instance!!.loadCollectionData()
            }
            return instance!!
        }
    }
}