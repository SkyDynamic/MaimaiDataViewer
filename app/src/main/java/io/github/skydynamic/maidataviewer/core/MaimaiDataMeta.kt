package io.github.skydynamic.maidataviewer.core

import androidx.lifecycle.viewModelScope
import io.github.skydynamic.maidataviewer.core.manager.UpdateDataManager
import io.github.skydynamic.maidataviewer.core.manager.collection.CollectionType
import io.github.skydynamic.maidataviewer.viewmodel.GlobalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


data class MaimaiDataMeta(
    var currentMaimaiDataVersion: MaiVersion,
    var latestMaimaiDataVersion: MaiVersion,
    var currentTitleDataVersion: MaiVersion,
    var latestTitleDataVersion: MaiVersion
) {
    fun isLatestMaimaiDataVersion(): Boolean {
        return currentMaimaiDataVersion >= latestMaimaiDataVersion
    }

    fun isLatestTitleDataVersion(): Boolean {
        return currentTitleDataVersion >= latestTitleDataVersion
    }

    fun updateMaimaiData(c: MaiVersion, l: MaiVersion) {
        currentMaimaiDataVersion = c
        latestMaimaiDataVersion = l
    }

    fun updateTitleData(c: MaiVersion, l: MaiVersion) {
        currentTitleDataVersion = c
        latestTitleDataVersion = l
    }

    companion object {
        private lateinit var instance: MaimaiDataMeta

        fun getInstance(): MaimaiDataMeta {
            return instance
        }

        fun init(onFinished: () -> Unit) {
            if (!this::instance.isInitialized) {
                GlobalViewModel.viewModelScope.launch(Dispatchers.IO) {
                    val current = UpdateDataManager.instance
                        .getCurrentUpdateData() ?: MaiVersion(-1, 0)

                    val latest = UpdateDataManager.instance
                        .getNetworkLatestUpdateData() ?: MaiVersion(-1, 0)

                    val currentTitle = CollectionType.TITLE.manager!!
                        .currentCollectionVersion

                    val latestTitle = CollectionType.TITLE.manager!!
                        .getLatestCollectionDataVersion() ?: MaiVersion(-1, 0)

                    instance = MaimaiDataMeta(
                        current,
                        latest,
                        currentTitle,
                        latestTitle
                    )
                    onFinished()
                }
            }
        }
    }
}