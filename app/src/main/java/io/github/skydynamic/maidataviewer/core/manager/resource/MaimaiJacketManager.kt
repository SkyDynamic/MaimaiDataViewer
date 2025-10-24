package io.github.skydynamic.maidataviewer.core.manager.resource

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.Log
import io.github.skydynamic.maidataviewer.core.mkdirsIfNotExists
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.File

class MaimaiJacketManager(
    override val assetManager: AssetManager,
    override val resPath: File,
    override val httpClient: AppHttpClient,
) : MaimaiResourceManager {
    val assetsUrl = "https://assets2.lxns.net/maimai/jacket"
    val baseApiUrl = "https://mdvu.skydynamic.top/api/v0/title"

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

    override suspend fun getResFile(id: Int): File? {
        var yid = id
        var aid = id
        if (id > 0) {
            if (id > 100000) {
                yid = id % 10000
            } else if (yid > 10000) {
                aid -= 10000
            }

            val jacketPath = resPath.resolve("jacket")
            jacketPath.mkdirsIfNotExists()

            val jacketFile = jacketPath.resolve("$yid.png")
            var result: Long? = null
            if (!jacketFile.exists() || checkFileBroken(jacketFile)) {
                jacketFile.deleteOnExit()
                result = httpClient.request {
                    it.get("$assetsUrl/$aid.png")
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

    override fun getResByteFromAssets(id: Int): ByteArray? {
        if (id >= 0) {
            val jacketByte = assetManager.open("jacket/$id.png").readBytes()
            return jacketByte
        } else {
            Log.e("MaimaiJacketManager", "Invalid jacket id: $id")
            return null
        }
    }

    companion object {
        fun build(
            assetsManager: AssetManager,
            resPath: File,
            httpClient: AppHttpClient
        ): MaimaiJacketManager {
            return MaimaiJacketManager(assetsManager, resPath, httpClient)
        }
    }
}