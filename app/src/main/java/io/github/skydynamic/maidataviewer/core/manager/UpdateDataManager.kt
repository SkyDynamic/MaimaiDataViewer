package io.github.skydynamic.maidataviewer.core.manager

import io.github.skydynamic.maidataviewer.core.MaiVersion
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

class UpdateDataManager(
    private val dataPath: File,
    private val httpClient: AppHttpClient
) {
    private val baseUrl = "https://mdvu.skydynamic.top/api/v0/"

    private var existsUpdates: List<MaiVersion> = getAllUpdateData()

    @Serializable
    data class NewVersionResponse(
        val latest: String
    )

    @Serializable
    data class UpdateDataResponse(
        val version: String,
        val data: List<MaimaiMusicData>,
        val appendUpgrade: Boolean,
        val requiredVersion: List<String>
    )

    fun getAllUpdateData(): List<MaiVersion> {
        val updates = dataPath.listFiles()?.filter {
            it.extension == "json" && MaiVersion.tryParse(it.nameWithoutExtension) != null
        }
        return updates?.map {
            MaiVersion.tryParse(it.nameWithoutExtension)!!
        } ?: emptyList()
    }

    fun getLatestUpdateData(): MaiVersion? {
        val updates = getAllUpdateData()
        return updates.maxOrNull()
    }

    suspend fun getNetworkLatestUpdateData(): MaiVersion? {
        val latestUpdateResp = httpClient.request {
            return@request it.get(baseUrl + "new")
        }

        val body = latestUpdateResp?.body<NewVersionResponse>()
        return MaiVersion.tryParse(body?.latest ?: "-1.0.0")
    }

    suspend fun updateData(
        target: MaiVersion,
        onFinished: (MaiVersion?) -> Unit
    ) {
        val updateDataResp = httpClient.request {
            return@request it.get(
                baseUrl
                        + "update?version=${target.toStandardString()}"
            )
        }

        val body = updateDataResp?.body<UpdateDataResponse>()
        if (body != null) {
            val version = MaiVersion.tryParse(body.version)!!
            val appendUpgrade = body.appendUpgrade
            val requiredVersion = body.requiredVersion

            if (!appendUpgrade) {
                existsUpdates.forEach {
                    dataPath.resolve(it.toStandardString() + ".json").delete()
                }

                dataPath.resolve(version.toStandardString() + ".json")
                    .writeText(Json.encodeToString(body))
            } else {
                val updateFile = dataPath.resolve(version.toStandardString() + ".json")
                if (updateFile.exists()) {
                    updateFile.delete()
                } else {
                    dataPath.resolve(version.toStandardString() + ".json")
                        .writeText(Json.encodeToString(body))

                    requiredVersion.forEach {
                        val v = MaiVersion.tryParse(it)!!
                        if (!existsUpdates.contains(v)) {
                            updateData(v) {}
                        }
                    }
                }
            }

            existsUpdates = getAllUpdateData()
            onFinished(version)
        } else {
            onFinished(null)
        }
    }

    companion object {
        lateinit var instance: UpdateDataManager

        fun init(
            dataPath: File,
            httpClient: AppHttpClient
        ) {
            instance = UpdateDataManager(dataPath, httpClient)
        }
    }
}