package io.github.skydynamic.maidataviewer.core.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppConfig(
    val context: Context,
) {
    private val preferences: Flow<Preferences> = context.dataStore.data

    fun <T> read(block: (Preferences) -> T): Flow<T> {
        return preferences.map {
            block(it)
        }
    }

    suspend fun update(block: (MutablePreferences) -> Unit) {
        context.dataStore.updateData {
            it.toMutablePreferences().also { preferences ->
                block(preferences)
            }
        }
    }

    companion object {
        val chooseNode = stringPreferencesKey("choose_node")
        val customNodeUrl = stringPreferencesKey("custom_node_url")
    }
}