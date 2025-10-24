package io.github.skydynamic.maidataviewer.core.manager.resource

import android.content.res.AssetManager
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import java.io.File

interface MaimaiResourceManager {
    val assetManager: AssetManager
    val resPath: File
    val httpClient: AppHttpClient

    suspend fun getResFile(id: Int): File?

    fun getResByteFromAssets(id: Int): ByteArray?

    companion object {
        fun initAll(
            assetManager: AssetManager,
            resPath: File,
            httpClient: AppHttpClient
        ) {
            ResourceManagerType.entries.forEach {
                it.instance = it.builder(assetManager, resPath, httpClient)
            }
        }
    }
}