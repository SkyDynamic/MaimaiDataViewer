package io.github.skydynamic.maidataviewer.core

import io.github.skydynamic.maidataviewer.core.manager.collection.CollectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class MaimaiVersionMeta{
    private var isInitialized: Boolean = false
    private var currentVersion: MaiVersion = MaiVersion(0, 0)
    private var latestVersion: MaiVersion = MaiVersion(0, 0)

    constructor()

    constructor(
        currentMaimaiVersion: MaiVersion,
        latestMaimaiVersion: MaiVersion
    ) {
        this.currentVersion = currentMaimaiVersion
        this.latestVersion = latestMaimaiVersion
    }

    fun initialize(manager: CollectionManager<*>) {
        if (!isInitialized) {
            CoroutineScope(Dispatchers.IO).async {
                currentVersion = manager.currentCollectionVersion
                latestVersion = manager.getLatestCollectionDataVersion() ?: MaiVersion(0, 0)
                isInitialized = true
            }
        }
    }

    fun isLatestMaimaiVersion(): Boolean {

        return currentVersion >= latestVersion
    }

    fun updateCurrentMaimaiVersion(version: MaiVersion) {
        currentVersion = version
    }

    fun updateLatestMaimaiVersion(version: MaiVersion) {
        latestVersion = version
    }

    fun getIsInitialized(): Boolean {
        return isInitialized
    }

    fun getCurrentVersionString(): String {
        return "$latestVersion" + " (${latestVersion.toStandardString()})"
    }
}