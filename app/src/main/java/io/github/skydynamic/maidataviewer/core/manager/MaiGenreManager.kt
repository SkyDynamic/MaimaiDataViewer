package io.github.skydynamic.maidataviewer.core.manager

import android.content.res.AssetManager
import android.util.Log
import io.github.skydynamic.maidataviewer.core.MaiVersion
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

enum class GenreType(
    val fileName: String,
    val updateUrl: String
) {
    MUSIC("music_genre.json", "genre"),
    VERSION("music_version.json", "version")
}

class MaiGenreManager(
    val type: GenreType,
    val assetsManager: AssetManager,
    val overrideDataPath: File,
    private val httpClient: AppHttpClient
) {
    private val baseUrl = "https://mdvu.skydynamic.top/api/v0/"

    @Serializable
    data class GenreData(
        val version: String,
        val data: List<MaiGenreData>
    ) {
        @Serializable
        data class MaiGenreData(
            val id: Int,
            val name: String
        )
    }

    fun getGenreData(): List<GenreData.MaiGenreData> {
        val assetsData = assetsManager.open(type.fileName).let {
            val assetsDataText = it.bufferedReader().readText()
            val assetsData = Json.decodeFromString<GenreData>(assetsDataText)
            assetsData
        }
        val overrideDataFile = overrideDataPath.resolve(type.fileName)
        if (overrideDataFile.exists()) {
            val overrideDataText = overrideDataFile.readText()
            val overrideData = Json.decodeFromString<GenreData>(overrideDataText)

            val assetsVersion = MaiVersion.tryParse(assetsData.version)!!
            val overrideVersion = MaiVersion.tryParse(overrideData.version)!!
            if (overrideVersion > assetsVersion) {
                return overrideData.data
            }
        }
        return assetsData.data
    }

    fun getGenreName(id: Int): String {
        return getGenreData().firstOrNull { it.id == id }?.name ?: ""
    }

    suspend fun updateData(
        onFinished: (MaiVersion?) -> Unit
    ) {
        val latestUpdateResp = httpClient.request {
            return@request it.get(baseUrl + type.updateUrl)
        }
        val body = latestUpdateResp?.body<GenreData>()

        if (body != null) {
            val version = MaiVersion.tryParse(body.version)!!
            val assetsData = assetsManager.open(type.fileName).let {
                val assetsDataText = it.bufferedReader().readText()
                val assetsData = Json.decodeFromString<GenreData>(assetsDataText)
                assetsData
            }
            if (version > MaiVersion.tryParse(assetsData.version)!!) {
                val overrideDataFile = overrideDataPath.resolve(type.fileName)
                overrideDataFile.writeText(Json.encodeToString(body))
                onFinished(version)
            } else {
                onFinished(null)
            }
        }
    }

    companion object {
        private lateinit var instances: Map<GenreType, MaiGenreManager>

        lateinit var musicGenre: MaiGenreManager
        lateinit var versionGenre: MaiGenreManager

        fun init(
            assetsPath: AssetManager,
            overrideDataPath: File,
            httpClient: AppHttpClient
        ) {
            instances = GenreType.entries.associateWith {
                MaiGenreManager(it, assetsPath, overrideDataPath, httpClient)
            }

            musicGenre = get(GenreType.MUSIC)
            versionGenre = get(GenreType.VERSION)
        }

        fun get(type: GenreType): MaiGenreManager {
            if (!::instances.isInitialized) {
                Log.e("MaiGenreManager", "MaiGenreManager not initialized")
            }
            if (!instances.containsKey(type)) {
                Log.e("MaiGenreManager", "MaiGenreManager: $type not found")
            }
            return instances[type]!!
        }
    }
}