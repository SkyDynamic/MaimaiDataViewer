package io.github.skydynamic.maidataviewer.ui.page.treasurebox.collection

import androidx.compose.runtime.Composable
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.manager.collection.CollectionType
import io.github.skydynamic.maidataviewer.core.manager.resource.ResourceManagerType
import io.github.skydynamic.maidataviewer.core.strings

object FramePageViewModel : BaseCollectionViewModel() {
    override fun getCollectionType() = CollectionType.FRAME
}

@Composable
fun FramePage(
    onPicked: Boolean = false,
    onBackPressed: () -> Unit,
) {
    CollectionPage(
        viewModel = FramePageViewModel,
        resourceManager = ResourceManagerType.FRAME,
        title = R.string.frame_page.strings,
        searchResultStringRes = R.string.frame_search_result.strings,
        onBackPressed = onBackPressed,
        onPicked = onPicked
    )
}