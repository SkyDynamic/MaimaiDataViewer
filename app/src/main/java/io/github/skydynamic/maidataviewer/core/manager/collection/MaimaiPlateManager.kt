package io.github.skydynamic.maidataviewer.core.manager.collection

import android.content.res.AssetManager
import android.util.Log
import io.github.skydynamic.maidataviewer.core.MaiVersion
import io.github.skydynamic.maidataviewer.core.data.MaimaiCommonCollectionData
import io.github.skydynamic.maidataviewer.core.manager.resource.MaimaiResourceManager
import io.github.skydynamic.maidataviewer.core.mkdirsIfNotExists
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import io.github.skydynamic.maidataviewer.core.network.ResourceNode
import io.github.skydynamic.maidataviewer.core.network.getUrl
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

class MaimaiPlateManager(
    override val assetManager: AssetManager,
    override val resPath: File,
    override val httpClient: AppHttpClient
) : MaimaiResourceManager, CollectionManager<MaimaiCommonCollectionData> {
    override var _isLoaded: Boolean = false
    override var _currentCollectionVersion: MaiVersion = MaiVersion(-1, 0)

    val assetsUrl
        get() = suspend { ResourceNode.getCurrentNode().getUrl("plate") }

    private var plateData: Map<Int, MaimaiCommonCollectionData> = emptyMap()

    override val collectionType: CollectionType = CollectionType.PLATE

    @Serializable
    data class PlateData(
        val version: String,
        val data: List<MaimaiCommonCollectionData>
    )

    override fun loadCollectionData() {
        val file = resPath.resolve("plate_list.json")
        if (file.exists()) {
            val json = file.readText()
            val plateData = Json.Default.decodeFromString<PlateData>(json)
            currentCollectionVersion = MaiVersion.Companion.tryParse(plateData.version)!!
            this.plateData = plateData.data.associateBy { it.id }
            isLoaded = true
        }
    }

    override fun getCollection(id: Int): MaimaiCommonCollectionData? {
        return plateData[id]
    }

    override fun search(
        keyword: String,
        filterAction: ((List<MaimaiCommonCollectionData>) -> List<MaimaiCommonCollectionData>)?
    ): List<MaimaiCommonCollectionData> {
        val result = plateData.values.filter {
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
            val plateData = httpClient.request {
                val response = it.get("${this.baseApiUrl}/update")
                response.body<PlateData>()
            }

            val latestVersion = MaiVersion.Companion.tryParse(plateData!!.version)
            if (latestVersion != null && latestVersion > currentCollectionVersion) {
                currentCollectionVersion = latestVersion
                resPath.resolve("plate_list.json")
                    .writeText(Json.Default.encodeToString(plateData))
            }

            onFinished(latestVersion)
        } catch (e: Exception) {
            Log.e("PlateDataManager", "Error downloading plate data", e)
        }
    }

    override suspend fun getResFile(id: Int): File? {
        val platePath = resPath.resolve("plate")
        platePath.mkdirsIfNotExists()

        val plateFile = platePath.resolve("$id.png")
        if (!plateFile.exists()) {
            httpClient.request {
                val request = it.get("${assetsUrl()}/$id.png")
                if (request.status.value == 200) {
                    request.bodyAsChannel()
                        .copyTo(plateFile.outputStream())
                } else {
                    Log.e("MaimaiPlateManager", "Error downloading name plate $id")
                }
            }
            Log.i("MaimaiPlateManager", "Downloaded plate $id")
        }
        return if (plateFile.exists()) {
            plateFile
        } else {
            null
        }
    }

    override fun getResByteFromAssets(id: Int): ByteArray? {
        if (id >= 0) {
            val plateByte = assetManager.open("plate/$id.png").readBytes()
            return plateByte
        } else {
            Log.e("MaimaiPlateManager", "Invalid plate id: $id")
            return null
        }
    }

    companion object {
        private var instance: MaimaiPlateManager? = null

        fun build(
            assetsManager: AssetManager,
            resPath: File,
            httpClient: AppHttpClient
        ): MaimaiPlateManager {
            if (instance == null) {
                instance = MaimaiPlateManager(assetsManager, resPath, httpClient)
            }
            if (!instance!!.isLoaded) {
                instance!!.loadCollectionData()
            }
            return instance!!
        }
    }
}