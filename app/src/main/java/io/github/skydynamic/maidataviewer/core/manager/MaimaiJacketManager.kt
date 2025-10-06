package io.github.skydynamic.maidataviewer.core.manager

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.Log
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.File

class MaimaiJacketManager(
    private val assetsManager: AssetManager,
    private val dataPath: File,
    private val httpClient: AppHttpClient
) {
    val assetsUrl = "https://assets2.lxns.net/maimai"

    private fun checkFileBroken(file: File): Boolean {
        val bitmap = BitmapFactory.decodeFile(file.toString())
        if (bitmap == null) {
            return true
        }
        try {
            bitmap.width
        } catch (e: Exception) {
            return true
        }

        return false
    }

    suspend fun getJacketFile(id: Int): File? {
        var yid = id
        var aid = id
        if (id > 0) {
            if (id > 100000) {
                yid = id % 10000
            } else if (yid > 10000) {
                aid -= 10000
            }
            val jacketFile = dataPath.resolve("$yid.png")
            var result: Long? = null
            if (!jacketFile.exists() || checkFileBroken(jacketFile)) {
                jacketFile.deleteOnExit()
                result = httpClient.request {
                    it.get("$assetsUrl/jacket/$aid.png")
                        .bodyAsChannel()
                        .copyTo(jacketFile.outputStream())
                }
                Log.i("MaimaiJacketManager", "Downloaded jacket $id")
            }
            return if (result != null || jacketFile.exists() || !checkFileBroken(jacketFile)) {
                jacketFile
            } else {
                null
            }
        } else {
            Log.e("MaimaiJacketManager", "Invalid jacket id: $id")
            return null
        }
    }

    fun getJacketFromAssets(id: Int): ByteArray? {
        if (id >= 0) {
            val jacketData = assetsManager.open("$id.png").readBytes()
            return jacketData
        } else {
            Log.e("MaimaiJacketManager", "Invalid jacket id: $id")
            return null
        }
    }

    companion object {
        lateinit var instance: MaimaiJacketManager

        fun init(
            assetsManager: AssetManager,
            dataPath: File,
            httpClient: AppHttpClient
        ) {
            instance = MaimaiJacketManager(assetsManager, dataPath, httpClient)
        }
    }
}