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
    var latestTitleDataVersion: MaiVersion,
    var currentIconDataVersion: MaiVersion,
    var latestIconDataVersion: MaiVersion,
    var currentPlateDataVersion: MaiVersion,
    var latestPlateDataVersion: MaiVersion
) {
    fun isLatestMaimaiDataVersion(): Boolean {
        return currentMaimaiDataVersion >= latestMaimaiDataVersion
    }

    fun isLatestTitleDataVersion(): Boolean {
        return currentTitleDataVersion >= latestTitleDataVersion
    }

    fun isLatestIconDataVersion(): Boolean {
        return currentIconDataVersion >= latestIconDataVersion
    }

    fun isLatestPlateDataVersion(): Boolean {
        return currentPlateDataVersion >= latestPlateDataVersion
    }

    fun updateMaimaiData(c: MaiVersion, l: MaiVersion) {
        currentMaimaiDataVersion = c
        latestMaimaiDataVersion = l
    }

    fun updateTitleData(c: MaiVersion, l: MaiVersion) {
        currentTitleDataVersion = c
        latestTitleDataVersion = l
    }

    fun updateIconData(c: MaiVersion, l: MaiVersion) {
        currentIconDataVersion = c
        latestIconDataVersion = l
    }

    fun updatePlateData(c: MaiVersion, l: MaiVersion) {
        currentPlateDataVersion = c
        latestPlateDataVersion = l
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

                    val currentIcon = CollectionType.ICON.manager!!
                        .currentCollectionVersion

                    val latestIcon = CollectionType.ICON.manager!!
                        .getLatestCollectionDataVersion() ?: MaiVersion(-1, 0)

                    val currentPlate = CollectionType.PLATE.manager!!
                        .currentCollectionVersion

                    val latestPlate = CollectionType.PLATE.manager!!
                        .getLatestCollectionDataVersion() ?: MaiVersion(-1, 0)

                    instance = MaimaiDataMeta(
                        current,
                        latest,
                        currentTitle,
                        latestTitle,
                        currentIcon,
                        latestIcon,
                        currentPlate,
                        latestPlate
                    )
                    onFinished()
                }
            }
        }
    }
}