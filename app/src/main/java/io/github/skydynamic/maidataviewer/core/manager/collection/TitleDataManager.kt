package io.github.skydynamic.maidataviewer.core.manager.collection

import android.content.res.AssetManager
import android.util.Log
import io.github.skydynamic.maidataviewer.core.MaiVersion
import io.github.skydynamic.maidataviewer.core.data.IMaimaiCollectionData
import io.github.skydynamic.maidataviewer.core.data.MaimaiTitleData
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

class TitleDataManager(
    private val dataFile: File,
    override val httpClient: AppHttpClient
) : CollectionManager {
    override var _isLoaded: Boolean = false
    override var _currentCollectionVersion: MaiVersion = MaiVersion(-1, 0)

    override val collectionType: CollectionType = CollectionType.TITLE

    private var titleData: Map<Int, MaimaiTitleData> = emptyMap()

    @Serializable
    data class TitleData(
        val version: String,
        val data: List<MaimaiTitleData>
    )

    override fun loadCollectionData() {
        val file = dataFile.resolve("title_list.json")
        if (file.exists()) {
            val json = file.readText()
            val titleData = Json.Default.decodeFromString<TitleData>(json)
            currentCollectionVersion = MaiVersion.Companion.tryParse(titleData.version)!!
            this.titleData = titleData.data.associateBy { it.id }
            isLoaded = true
        }
    }

    override fun search(
        keyword: String,
        filterAction: ((List<IMaimaiCollectionData>) -> List<IMaimaiCollectionData>)?
    ): List<IMaimaiCollectionData> {
        val result = titleData.values.filter {
            it.name?.contains(keyword) == true
        }

        if (filterAction != null) {
            return filterAction(result)
        }

        return result
    }

    override suspend fun downloadCollectionData(
        onFinished: (MaiVersion?) -> Unit
    ) {
        try {
            val titleData = httpClient.request {
                val response = it.get("$baseApiUrl/update")
                response.body<CollectionManager.CollectionData>()
            }

            val latestVersion = MaiVersion.Companion.tryParse(titleData!!.version)
            if (latestVersion != null && latestVersion > currentCollectionVersion) {
                currentCollectionVersion = latestVersion
                dataFile.resolve("title_list.json")
                    .writeText(Json.Default.encodeToString(titleData))
            }

            onFinished(latestVersion)
        } catch (e: Exception) {
            Log.e("TitleDataManager", "Error downloading title data", e)
        }
    }

    companion object {
        fun init(
            assetManager: AssetManager,
            dataFile: File,
            httpClient: AppHttpClient
        ): TitleDataManager {
            val instance = TitleDataManager(dataFile, httpClient)
            instance.loadCollectionData()
            return instance
        }
    }
}