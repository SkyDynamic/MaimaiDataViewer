package io.github.skydynamic.maidataviewer.core.manager.collection

import android.content.res.AssetManager
import io.github.skydynamic.maidataviewer.core.MaiVersion
import io.github.skydynamic.maidataviewer.core.data.IMaimaiCollectionData
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import java.io.File

interface CollectionManager<T : IMaimaiCollectionData> {
    val collectionType: CollectionType
    val httpClient: AppHttpClient

    val baseApiUrl: String
        get() = "https://mdvu.skydynamic.top/api/v0/${collectionType.apiPoint}"

    var _isLoaded: Boolean

    var isLoaded: Boolean
        get() = _isLoaded
        set(value) {
            _isLoaded = value
        }

    var _currentCollectionVersion: MaiVersion

    var currentCollectionVersion: MaiVersion
        get() = _currentCollectionVersion
        set(value) {
            _currentCollectionVersion = value
        }

    @Serializable
    data class NewVersionResponse(
        val version: String
    )

    fun loadCollectionData()

    fun search(
        keyword: String,
        filterAction: ((List<T>) -> List<T>)?
    ): List<T> {
        throw NotImplementedError()
    }

    fun getCollection(id: Int): T?

    suspend fun getLatestCollectionDataVersion(): MaiVersion? {
        return httpClient.request {
            val response = it.get("$baseApiUrl/get")
            val json = response.body<NewVersionResponse>()
            MaiVersion.Companion.tryParse(json.version)
        }
    }

    suspend fun downloadCollectionData(onFinished: (MaiVersion?) -> Unit)

    companion object {
        fun initAll(
            assetManager: AssetManager,
            resPath: File,
            httpClient: AppHttpClient
        ) {
            CollectionType.entries.forEach {
                it.manager = it.builder(assetManager, resPath, httpClient)
            }
        }
    }
}