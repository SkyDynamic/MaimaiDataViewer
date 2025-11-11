package io.github.skydynamic.maidataviewer.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.MaimaiVersionMeta
import io.github.skydynamic.maidataviewer.core.manager.collection.CollectionManager
import io.github.skydynamic.maidataviewer.core.manager.collection.CollectionType
import io.github.skydynamic.maidataviewer.core.strings
import io.github.skydynamic.maidataviewer.ui.component.text.AnimatedTextTitleGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Suppress("unused")
class MaimaiDataUpdateState(
    val type: MaimaiDataType,
) : ViewModel() {
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _shouldUpdate = MutableStateFlow(false)
    val shouldUpdate: StateFlow<Boolean> = _shouldUpdate.asStateFlow()

    private val _isLatest = MutableStateFlow(false)
    val isLatest: StateFlow<Boolean> = _isLatest.asStateFlow()

    private var updateJob: Deferred<*>? = null

    init {
        _shouldUpdate.value = shouldUpdate()
        _isLatest.value = type.versionMeta.isLatestMaimaiVersion()
    }

    fun update(): Deferred<*> {
        return viewModelScope.async(Dispatchers.IO) {
            _isUpdating.value = true
            _shouldUpdate.value = false
            type.manager.downloadCollectionData {
                if (it != null) {
                    type.versionMeta.updateCurrentMaimaiVersion( it)
                }
                _isUpdating.value = false
                _shouldUpdate.value = shouldUpdate()
                _isLatest.value = type.versionMeta.isLatestMaimaiVersion()
            }
        }.also {
            updateJob = it
        }
    }

    fun cancelUpdate() {
        updateJob?.cancel()
    }

    fun shouldUpdate(): Boolean {
        return !type.versionMeta.isLatestMaimaiVersion()
    }

    fun setShouldUpdate(shouldUpdate: Boolean) {
        _shouldUpdate.value = shouldUpdate
    }

    fun setIsLatest(isLatest: Boolean) {
        _isLatest.value = isLatest
    }
}

class MaimaiDataUpdateStateFactory(private val type: MaimaiDataType) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaimaiDataUpdateState::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MaimaiDataUpdateState(type) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

enum class MaimaiDataType(
    val title: String,
    val manager: CollectionManager<*>,
    val versionMeta: MaimaiVersionMeta
) {
    TITLE(
        R.string.title_version.strings,
        CollectionType.TITLE.manager!!,
        MaimaiVersionMeta()
    ),
    ICON(
        R.string.icon_version.strings,
        CollectionType.ICON.manager!!,
        MaimaiVersionMeta()
    ),
    PLATE(
        R.string.plate_version.strings,
        CollectionType.PLATE.manager!!,
        MaimaiVersionMeta()
    ),
    FRAME(
        R.string.frame_version.strings,
        CollectionType.FRAME.manager!!,
        MaimaiVersionMeta()
    )
}

@Composable
private fun MaimaiDataItem(
    title: String,
    state: MaimaiDataUpdateState
) {
    val waitingSubtitleList = listOf(
        "w(ﾟДﾟ)w",
        "Σ( ° △ °|||)︴",
        "φ(≧ω≦*)♪"
    )
    val isVersionMetaInit by remember { mutableStateOf(state.type.versionMeta.getIsInitialized()) }
    var subtitle by remember { mutableStateOf("") }

    val isUpdating by state.isUpdating.collectAsState()
    val shouldUpdate by state.shouldUpdate.collectAsState()
    val isLatest by state.isLatest.collectAsState()

    LaunchedEffect(Unit) {
        if (!isVersionMetaInit) {
            state.type.versionMeta.initialize(state.type.manager)

            CoroutineScope(Dispatchers.IO).async {
                var i = 0
                while (!state.type.versionMeta.getIsInitialized()) {
                    subtitle = waitingSubtitleList[i]
                    i = (i + 1) % waitingSubtitleList.size

                    delay(500)
                }
                subtitle = state.type.versionMeta.getCurrentVersionString()
                state.setShouldUpdate(state.shouldUpdate())
                state.setIsLatest(state.type.versionMeta.isLatestMaimaiVersion())
            }
        } else {
            subtitle = state.type.versionMeta.getCurrentVersionString()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedTextTitleGroup(
                title = title,
                titleStyle = MaterialTheme.typography.titleSmall,
                subtitle = subtitle,
                subtitleColor = if (!isLatest) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                subtitleWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Button(
                onClick = {
                    state.update()
                },
                enabled = shouldUpdate
            ) {
                if (isUpdating) {
                    Text(
                        text = R.string.cancel.strings,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = R.string.update.strings,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun MaimaiDataManagerPage(
    onBackPressed: () -> Unit
) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = Icons.Default.Close.name,
                        modifier = Modifier.height(24.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = R.string.data_manager_page.strings,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(
                    count = MaimaiDataType.entries.size,
                    key = { MaimaiDataType.entries[it].name }
                ) {
                    val item = MaimaiDataType.entries[it]

                    if (it != 0) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }

                    MaimaiDataItem(
                        title = item.title,
                        state = viewModel<MaimaiDataUpdateState>(
                            key = item.name,
                            factory = MaimaiDataUpdateStateFactory(item)
                        )
                    )
                }
            }
        }
    }
}