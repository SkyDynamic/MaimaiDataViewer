package io.github.skydynamic.maidataviewer.core.manager

import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.core.data.MaimaiUpdateData
import io.github.skydynamic.maidataviewer.viewmodel.SongPageViewModel
import kotlinx.serialization.json.Json
import java.io.File

class MusicDataManager(
    private val musicDataDir: File
) {
    private val musicDataList: ArrayList<MaimaiMusicData> = arrayListOf()

    private var isLoaded = false

    fun loadMusicData(onFinished: (Int) -> Unit) {
        val musicDataFiles = UpdateDataManager.instance.getAllUpdateData()

        for (file in musicDataFiles) {
            val musicData = Json.decodeFromString<MaimaiUpdateData>(
                musicDataDir.resolve(file.toStandardString() + ".json").readText()
            )

            musicData.data.forEach { newItem ->
                val existingIndex = musicDataList.indexOfFirst { it.id == newItem.id }

                if (existingIndex != -1) {
                    musicDataList[existingIndex] = newItem
                } else {
                    musicDataList += newItem
                }
            }
        }
        musicDataList.sortBy { it.id }

        isLoaded = true

        onFinished(musicDataList.size)
    }

    fun getMusicData(id: Int): MaimaiMusicData? {
        return musicDataList.firstOrNull { it.id == id }
    }

    fun getIsLoaded(): Boolean {
        return isLoaded
    }

    fun reloadMusicData(onFinished: (Int) -> Unit) {
        musicDataList.clear()
        loadMusicData(onFinished)
    }

    fun searchMusicData(
        keyword: String,
        genreId: Int? = null,
        versionId: Int? = null
    ): List<MaimaiMusicData> {
        var result = musicDataList.filter {
            (it.name?.lowercase()?.contains(keyword.lowercase()) == true
                    || it.artist?.lowercase()?.contains(keyword.lowercase()) == true)
                    || it.id.toString() == keyword
        }.toMutableList()

        MusicAliasManager.getMusicByAlias(
            SongPageViewModel.searchText.value
        ).forEach {
            if (!result.contains(it)) result.add(it)
        }

        if (genreId != null) {
            result = result.filter {
                it.genre == genreId
            }.toMutableList ()
        }

        if (versionId != null) {
            result = if (versionId >= 13 && versionId % 2 == 0) {
                result.filter {
                    it.addVersion.id == versionId || it.addVersion.id == versionId + 1
                }.toMutableList ()
            } else {
                result.filter {
                    it.addVersion.id == versionId
                }.toMutableList ()
            }
        }
        return result
    }

    fun existsMusic(id: Int): Boolean {
        return musicDataList.any { it.id == id }
    }

    companion object {
        lateinit var instance: MusicDataManager

        fun init(musicDataDir: File) {
            instance = MusicDataManager(musicDataDir)
        }

        fun exists(id: Int): Boolean {
            return instance.existsMusic(id)
        }

        fun getMusicData(id: Int): MaimaiMusicData? {
            return instance.getMusicData(id)
        }
    }
}