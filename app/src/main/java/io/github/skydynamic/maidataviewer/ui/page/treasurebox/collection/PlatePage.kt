package io.github.skydynamic.maidataviewer.ui.page.treasurebox.collection

import androidx.compose.runtime.Composable
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.manager.collection.CollectionType
import io.github.skydynamic.maidataviewer.core.manager.resource.ResourceManagerType
import io.github.skydynamic.maidataviewer.core.strings

object PlatePageViewModel : BaseCollectionViewModel() {
    override fun getCollectionType() = CollectionType.PLATE
}

@Composable
fun PlatePage(
    onPicked: Boolean = false,
    onBackPressed: () -> Unit
) {
    CollectionPage(
        viewModel = PlatePageViewModel,
        resourceManager = ResourceManagerType.PLATE,
        title = R.string.plate_page.strings,
        searchResultStringRes = R.string.plate_search_result.strings,
        onBackPressed = onBackPressed,
        onPicked = onPicked
    )
}