package io.github.skydynamic.maidataviewer.core.manager.collection

import android.content.res.AssetManager
import io.github.skydynamic.maidataviewer.core.data.IMaimaiCollectionData
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import java.io.File

@Suppress("UNCHECKED_CAST")
enum class CollectionType(
    val apiPoint: String,
    val builder: (
        assetManager: AssetManager,
        resPath: File,
        httpClient: AppHttpClient) -> CollectionManager<*>,
    var manager: CollectionManager<*>?
) {
    TITLE("title", TitleDataManager::init, null),
    ICON("icon", MaimaiIconManager::build, null),
    PLATE("plate", MaimaiPlateManager::build, null);

    fun <T : IMaimaiCollectionData> getTypedManager(): CollectionManager<T>? {
        return manager as CollectionManager<T>?
    }
}