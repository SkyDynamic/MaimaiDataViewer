package io.github.skydynamic.maidataviewer.ui.page.treasurebox.collection

import androidx.compose.runtime.Composable
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.manager.collection.CollectionType
import io.github.skydynamic.maidataviewer.core.manager.resource.ResourceManagerType
import io.github.skydynamic.maidataviewer.core.strings

object IconPageViewModel : BaseCollectionViewModel() {
    override fun getCollectionType() = CollectionType.ICON
}

@Composable
fun IconPage(onBackPressed: () -> Unit) {
    CollectionPage(
        viewModel = IconPageViewModel,
        resourceManager = ResourceManagerType.ICON,
        title = R.string.icon_page.strings,
        searchResultStringRes = R.string.icon_search_result.strings,
        onBackPressed = onBackPressed
    )
}