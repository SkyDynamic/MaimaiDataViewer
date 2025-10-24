package io.github.skydynamic.maidataviewer.ui.page.treasurebox

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.skydynamic.maidataviewer.core.data.MaimaiIconData
import kotlinx.coroutines.Job

object IconPageViewModel : ViewModel() {
    var isLoaded by mutableStateOf(false)

    var searchText by mutableStateOf("")

    var filterRate by mutableIntStateOf(-1)

    var isSearching by mutableStateOf(false)

    var isSearchingActive by mutableStateOf(false)

    var searchJob by mutableStateOf<Job?>(null)

    var searchResult by mutableStateOf<List<MaimaiIconData>>(emptyList())

    var listState by mutableStateOf<LazyListState?>(null)
}

@Composable
fun IconPage(
    onBackPressed: () -> Unit
) {
    Surface {

    }
}