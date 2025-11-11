package io.github.skydynamic.maidataviewer.ui.page.treasurebox.collection

import androidx.compose.runtime.Composable
import io.github.skydynamic.maidataviewer.R
import io.github.skydynamic.maidataviewer.core.strings

enum class CollectionPages(
    val pageName: String,
    val desc: String,
    var page: String,
    val pageComposable: @Composable (
            backPressed: () -> Any
    ) -> Unit
) {
    TITLE_PAGE(
        R.string.title_page.strings,
        R.string.title_page_desc.strings,
        "titlePage",
        { backPressed ->
            TitlePage {
                backPressed()
            }
        }
    ),
    ICON_PAGE(
        R.string.icon_page.strings,
        R.string.icon_page_desc.strings,
        "iconPage",
        { backPressed ->
            IconPage {
                backPressed()
            }
        }
    ),
    PLATE_PAGE(
        R.string.plate_page.strings,
        R.string.plate_page_desc.strings,
        "platePage",
        { backPressed ->
            PlatePage {
                backPressed()
            }
        }
    ),
    FRAME_PAGE(
        R.string.frame_page.strings,
        R.string.frame_page_desc.strings,
        "framePage",
        { backPressed ->
            FramePage {
                backPressed()
            }
        }
    )
}