package io.github.skydynamic.maidataviewer.core.manager

import io.github.skydynamic.maidataviewer.core.data.Difficulty
import io.github.skydynamic.maidataviewer.core.data.MaimaiMusicData
import io.github.skydynamic.maidataviewer.core.data.MaimaiUpdateData
import io.github.skydynamic.maidataviewer.viewmodel.MusicPageViewModel
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.min

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
        versionId: Int? = null,
        levelRange: ClosedFloatingPointRange<Float>? = null,
        targetDifficulty: List<Difficulty>? = null
    ): List<MaimaiMusicData> {
        var result = musicDataList.filter {
            (it.name?.lowercase()?.contains(keyword.lowercase()) == true
                    || it.artist?.lowercase()?.contains(keyword.lowercase()) == true)
                    || it.id.toString() == keyword
        }.toMutableList()

        MusicAliasManager.getMusicByAlias(
            MusicPageViewModel.searchText.value
        ).forEach {
            if (!result.contains(it)) result.add(it)
        }

        val targetDiffMap = targetDifficulty?.map { it.ordinal } ?: emptyList()

        if (levelRange != null) {
            val minLevel = "%.1f".format(levelRange.start).toFloat()
            val maxLevel = "%.1f".format(levelRange.endInclusive).toFloat()

            result = result.filter {
                it.difficulties.filterIndexed { index, _ ->
                    if (targetDiffMap.isNotEmpty()) {
                        index in targetDiffMap
                    } else {
                        true
                    }
                }.any { difficulty ->
                    difficulty.level >= minLevel && difficulty.level <= maxLevel
                }
            }.toMutableList ()
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

    fun getMusicLoadedSize(): Int {
        return musicDataList.size
    }

    fun randomMusic(
        genreId: Int? = null,
        versionId: Int? = null,
        minLevel: Float = 0.0F,
        maxLevel: Float = 99.9F,
        count: Int = 1
    ): List<MaimaiMusicData> {
        var filteredMusic = musicDataList.filter {
            it.difficulties.any { difficulty ->
                        difficulty.level >= minLevel && difficulty.level <= maxLevel
            }
        }

        if (genreId != null) {
            filteredMusic = filteredMusic.filter {
                it.genre == genreId
            }
        }

        if (versionId != null) {
            filteredMusic = if (versionId >= 13 && versionId % 2 == 0) {
                filteredMusic.filter {
                    it.addVersion.id == versionId || it.addVersion.id == versionId + 1
                }
            } else {
                filteredMusic.filter {
                    it.addVersion.id == versionId
                }
            }
        }

        filteredMusic = filteredMusic.filter {
            it.id <= 100000
        }

        return filteredMusic.shuffled().take(count)
    }

    fun getMusicIdList(): List<Int> {
        return musicDataList.map { it.id }
    }

    companion object {
        private var _instance: MusicDataManager? = null

        val instance: MusicDataManager
            get() {
                return _instance ?:
                throw IllegalStateException("MusicDataManager is not initialized")
            }

        fun init(musicDataDir: File) {
            _instance = MusicDataManager(musicDataDir)
        }

        fun exists(id: Int): Boolean {
            return instance.existsMusic(id)
        }

        fun getMusicData(id: Int): MaimaiMusicData? {
            return instance.getMusicData(id)
        }

        fun getMusicLoadedSize(): Int {
            return instance.getMusicLoadedSize()
        }

        fun randomMusic(
            genreId: Int? = null,
            versionId: Int? = null,
            minLevel: Float = 0.0F,
            maxLevel: Float = 99.9F,
            count: Int = 1
        ): List<MaimaiMusicData> {
            return instance.randomMusic(genreId, versionId, minLevel, maxLevel, count)
        }
    }
}