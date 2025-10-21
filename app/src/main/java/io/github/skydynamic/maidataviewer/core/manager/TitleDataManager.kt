package io.github.skydynamic.maidataviewer.core.manager

import android.util.Log
import io.github.skydynamic.maidataviewer.core.MaiVersion
import io.github.skydynamic.maidataviewer.core.data.MaimaiTitleData
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

class TitleDataManager(
    private val dataFile: File,
    private val httpClient: AppHttpClient
) {
    var isLoaded = false

    private var titleDataVersion = MaiVersion(-1, 0)

    private var titleData: Map<Int, MaimaiTitleData> = emptyMap()

    val baseApiUrl = "https://mdvu.skydynamic.top/api/v0/title"

    @Serializable
    data class NewVersionResponse(
        val version: String
    )

    @Serializable
    data class TitleDataResponse(
        val version: String,
        val data: List<MaimaiTitleData>
    )

    fun loadTitleData() {
        val file = dataFile.resolve("title_list.json")
        if (file.exists()) {
            val json = file.readText()
            val titleData = Json.decodeFromString<TitleDataResponse>(json)
            titleDataVersion = MaiVersion.tryParse(titleData.version)!!
            this.titleData = titleData.data.associateBy { it.id }
            isLoaded = true
        }
    }

    fun getTitleDataVersion(): MaiVersion {
        return titleDataVersion
    }

    fun search(keyword: String, rareId: Int? = null): List<MaimaiTitleData> {
        var result = titleData.values.filter {
            it.name?.contains(keyword) == true
        }

        if (rareId != null) {
            result = result.filter {
                it.rareType == MaimaiTitleData.RareType.entries[rareId]
            }
        }

        return result
    }

    suspend fun getLatestUpdateData(): MaiVersion? {
        return httpClient.request {
            val response = it.get("$baseApiUrl/get")
            val json = response.body<NewVersionResponse>()
            MaiVersion.tryParse(json.version)
        }
    }

    suspend fun downloadTitleData(
        onFinished: (MaiVersion?) -> Unit
    ) {
        try {
            val titleData = httpClient.request {
                val response = it.get("$baseApiUrl/update")
                response.body<TitleDataResponse>()
            }

            val currentVersion = MaiVersion.tryParse(titleData!!.version)
            if (currentVersion != null && currentVersion > titleDataVersion) {
                titleDataVersion = currentVersion
                dataFile.resolve("title_list.json")
                    .writeText(Json.encodeToString(titleData))
            }

            onFinished(currentVersion)
        } catch (e: Exception) {
            Log.e("TitleDataManager", "Error downloading title data", e)
        }
    }

    companion object {
        lateinit var instance: TitleDataManager

        fun init(
            dataFile: File,
            httpClient: AppHttpClient
        ) {
            instance = TitleDataManager(dataFile, httpClient)
            instance.loadTitleData()
        }

        fun isLoaded(): Boolean {
            return instance.isLoaded
        }
    }
}