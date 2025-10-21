package io.github.skydynamic.maidataviewer

import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.net.toUri
import io.github.skydynamic.maidataviewer.core.manager.MaiGenreManager
import io.github.skydynamic.maidataviewer.core.manager.MaimaiJacketManager
import io.github.skydynamic.maidataviewer.core.manager.MusicAliasManager
import io.github.skydynamic.maidataviewer.core.manager.MusicDataManager
import io.github.skydynamic.maidataviewer.core.manager.TitleDataManager
import io.github.skydynamic.maidataviewer.core.manager.UpdateDataManager
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import java.io.FileOutputStream
import java.io.InputStream

class Application : Application() {
    val appHttpClient: AppHttpClient = AppHttpClient()

    val layoutDirection: LayoutDirection
        @Composable get() =
            if (LocalConfiguration.current.layoutDirection == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

    override fun onCreate() {
        super.onCreate()
        application = this

        val updatePath = this.filesDir.resolve("update")
        if (!updatePath.exists()) {
            updatePath.mkdirs()
        }
        UpdateDataManager.init(
            updatePath,
            this.appHttpClient
        )

        TitleDataManager.init(
            this.filesDir,
            this.appHttpClient
        )
        TitleDataManager.instance.loadTitleData()

        MaiGenreManager.init(
            this.assets,
            this.filesDir,
            this.appHttpClient
        )

        MusicDataManager.init(this.filesDir.resolve("update"))

        val jacketPath = this.filesDir.resolve("jacket")
        if (!jacketPath.exists()) {
            jacketPath.mkdirs()
        }
        MaimaiJacketManager.init(
            this.assets,
            jacketPath,
            this.appHttpClient
        )

        MusicAliasManager.init(
            this.filesDir.resolve("music_alias.json"),
            this.appHttpClient
        )
    }

    private fun getFilesDirInputStream(fileName: String): InputStream {
        return this.openFileInput(fileName)
    }

    fun getFilesDirOutputStream(fileName: String): FileOutputStream {
        return this.openFileOutput(fileName, MODE_PRIVATE)
    }

    fun <T> readFile(fileName: String, block: (InputStream) -> T): T {
        return block(getFilesDirInputStream(fileName))
    }

    fun saveImageToGallery(
        image: ByteArray,
        fileName: String
    ) {
        val resolver = contentResolver
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + "/MaiDataViewer")

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        resolver.openOutputStream(uri!!).use {
            it?.write(image)
            Toast.makeText(this, R.string.save_jacket_success, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openBilibiliSearch(
        keyword: String
    ) {
        val uri = "bilibili://search?keyword=$keyword".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (isPackageInstalled("tv.danmaku.bili")) {
            startActivity(intent)
        } else {
            val uri = "https://search.bilibili.com/all?keyword=$keyword".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    companion object {
        lateinit var application: io.github.skydynamic.maidataviewer.Application
    }
}