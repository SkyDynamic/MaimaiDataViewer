package io.github.skydynamic.maidataviewer.core.manager.resource

import android.content.res.AssetManager
import io.github.skydynamic.maidataviewer.core.manager.collection.MaimaiIconManager
import io.github.skydynamic.maidataviewer.core.network.AppHttpClient
import java.io.File

enum class ResourceManagerType(
    val builder: (
        assetManager: AssetManager,
        resPath: File,
        httpClient: AppHttpClient) -> MaimaiResourceManager,
    var instance: MaimaiResourceManager?
) {
    JACKET(MaimaiJacketManager::build, null),
    ICON(MaimaiIconManager::build, null);
}