package io.github.skydynamic.maidataviewer.ui.page.treasurebox.collection

import androidx.compose.runtime.Composable
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.strings

enum class CollectionPages(
    val pageName: String,
    val desc: String,
    var page: String,
    val pageComposable: @Composable (
            backPressed: () -> Any,
            onPick: Boolean
    ) -> Unit
) {
    TITLE_PAGE(
        R.string.title_page.strings,
        R.string.title_page_desc.strings,
        "titlePage",
        { backPressed, onPick ->
            TitlePage(onPicked = onPick) {
                backPressed()
            }
        }
    ),
    ICON_PAGE(
        R.string.icon_page.strings,
        R.string.icon_page_desc.strings,
        "iconPage",
        { backPressed, onPick ->
            IconPage(onPicked = onPick) {
                backPressed()
            }
        }
    ),
    PLATE_PAGE(
        R.string.plate_page.strings,
        R.string.plate_page_desc.strings,
        "platePage",
        { backPressed, onPick ->
            PlatePage(onPicked = onPick) {
                backPressed()
            }
        }
    ),
    FRAME_PAGE(
        R.string.frame_page.strings,
        R.string.frame_page_desc.strings,
        "framePage",
        { backPressed, onPick ->
            FramePage(onPicked = onPick) {
                backPressed()
            }
        }
    ),
    SUB_MONITOR_PREVIEW_PAGE(
        R.string.submonitor_page.strings,
        R.string.submonitor_page_desc.strings,
        "subMonitorPreviewPage",
        { backPressed, _ ->
            SubMonitorPreviewPage {
                backPressed()
            }
        }
    )
}