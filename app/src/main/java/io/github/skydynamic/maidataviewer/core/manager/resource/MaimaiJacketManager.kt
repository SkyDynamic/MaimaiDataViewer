package io.github.skydynamic.maidataviewer.core.manager.resource

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.Log
import io.github.skydynamic.maidataviewer.core.mkdirsIfNotExists
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import io.github.skydynamic.maidataviewer.core.network.ResourceNode
import io.github.skydynamic.maidataviewer.core.network.getUrl
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File

class MaimaiJacketManager(
    override val assetManager: AssetManager,
    override val resPath: File,
    override val httpClient: AppHttpClient,
) : MaimaiResourceManager {
    val assetsUrl
        get() = suspend { ResourceNode.getCurrentNode().getUrl("jacket") }

    private fun checkFileBroken(file: File): Boolean {
        val bitmap = BitmapFactory.decodeFile(file.toString())
        if (bitmap == null) {
            return true
        }
        try {
            bitmap.width
        } catch (_: Exception) {
            return true
        }

        return false
    }

    override suspend fun getResFile(id: Int): File? {
        if (id <= 0) {
            Log.e("MaimaiJacketManager", "Invalid jacket id: $id")
            return null
        }

        val idStr = id.toString()
        val suffix = if (idStr.length >= 5) {
            idStr.substring(idStr.length - 4)
        } else {
            idStr
        }

        val numericSuffix = suffix.toInt()

        val jacketPath = resPath.resolve("jacket")
        jacketPath.mkdirsIfNotExists()

        val jacketFile = jacketPath.resolve("$suffix.png")
        var result: Long? = null

        if (!jacketFile.exists() || checkFileBroken(jacketFile)) {
            jacketFile.deleteOnExit()
            result = httpClient.request {
                it.get("${assetsUrl()}/$numericSuffix.png")
                    .bodyAsChannel()
                    .copyTo(jacketFile.outputStream())
            }
            Log.i("MaimaiJacketManager", "Downloaded jacket $id -> $numericSuffix")
        }

        return if (result != null || jacketFile.exists() && !checkFileBroken(jacketFile)) {
            jacketFile
        } else {
            null
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