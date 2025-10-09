package io.github.skydynamic.maidataviewer.core.manager

import io.github.skydynamic.maidataviewer.core.data.MaimaiAliasData
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

class MusicAliasManager(
    private val dataFile: File,
    private val httpClient: AppHttpClient
) {
    private var aliases: Map<Int, List<String>> = emptyMap()
    private var isLoaded = false

    val baseApiUrl = "https://maimai.lxns.net/api/v0/maimai/alias/list"

    @Serializable
    data class LxnsAliasApiResponse(
        val aliases: List<Alias>
    ) {
        @Serializable
        data class Alias(
            @SerialName("song_id") val songId: Int,
            val aliases: List<String>
        )
    }

    suspend fun loadAliasData() {
        var aliases: ArrayList<MaimaiAliasData> = arrayListOf()

        if (dataFile.exists()) {
            aliases = Json.decodeFromString<ArrayList<MaimaiAliasData>>(dataFile.readText())
        }

        if (aliases.isEmpty()) {
            val respData = httpClient.request {
                it.get(baseApiUrl).body<LxnsAliasApiResponse>().aliases
            }

            respData?.forEach {
                if (MusicDataManager.exists(it.songId)) {
                    aliases += MaimaiAliasData(
                        id = it.songId,
                        aliases = it.aliases
                    )
                }
                if (MusicDataManager.exists(it.songId + 10000)) {
                    aliases += MaimaiAliasData(
                        id = it.songId + 10000,
                        aliases = it.aliases
                    )
                }
            }

            dataFile.writeText(Json.encodeToString(aliases))
        }

        isLoaded = true

        this.aliases = aliases.associate {
            it.id to it.aliases
        }
    }

    fun getMusicByAlias(
        key: String
    ): List<MaimaiMusicData> {
        return aliases.filter {
            it.value.any { alias -> alias.lowercase().contains(key.lowercase()) }
        }.mapNotNull {
            MusicDataManager.getMusicData(it.key)
        }
    }

    suspend fun updateAliasData() {
        val aliases: ArrayList<MaimaiAliasData> = arrayListOf()

        val respData = httpClient.request {
            it.get(baseApiUrl).body<LxnsAliasApiResponse>().aliases
        }

        respData?.forEach {
            if (MusicDataManager.exists(it.songId)) {
                aliases += MaimaiAliasData(
                    id = it.songId,
                    aliases = it.aliases
                )
            }
            if (MusicDataManager.exists(it.songId + 10000)) {
                aliases += MaimaiAliasData(
                    id = it.songId + 10000,
                    aliases = it.aliases
                )
            }
        }

        dataFile.writeText(Json.encodeToString(aliases))

        this.aliases = aliases.associate {
            it.id to it.aliases
        }
    }

    fun getIsLoaded(): Boolean {
        return isLoaded
    }

    companion object {
        lateinit var instance: MusicAliasManager

        fun init(
            dataFile: File,
            httpClient: AppHttpClient
        ) {
            instance = MusicAliasManager(dataFile, httpClient)
        }

        fun getMusicByAlias(
            key: String
        ): List<MaimaiMusicData> {
            return instance.getMusicByAlias(key)
        }
     }
}