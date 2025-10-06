package io.github.skydynamic.maidataviewer.core

import androidx.lifecycle.viewModelScope
import io.github.skydynamic.maidataviewer.core.manager.UpdateDataManager
import io.github.skydynamic.maidataviewer.viewmodel.GlobalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


data class MaimaiDataMeta(
    var currentMaimaiDataVersion: MaiVersion,
    var latestMaimaiDataVersion: MaiVersion
) {
    fun isLatestMaimaiDataVersion(): Boolean {
        return currentMaimaiDataVersion >= latestMaimaiDataVersion
    }

    fun update(c: MaiVersion, l: MaiVersion) {
        currentMaimaiDataVersion = c
        latestMaimaiDataVersion = l
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
                        .getLatestUpdateData() ?: MaiVersion(-1, 0)

                    val latest = UpdateDataManager.instance
                        .getNetworkLatestUpdateData() ?: MaiVersion(-1, 0)

                    instance = MaimaiDataMeta(current, latest)
                    onFinished()
                }
            }
        }
    }
}